package com.voicerep.app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerep.app.audio.AudioEngine
import com.voicerep.app.audio.AudioEngineState
import com.voicerep.app.audio.AudioEvent
import com.voicerep.app.audio.AudioRecordHelper
import com.voicerep.app.data.local.entity.SetEntity
import com.voicerep.app.data.local.entity.WorkoutEntity
import com.voicerep.app.data.repository.WorkoutRepository
import com.voicerep.app.feedback.FeedbackManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutSessionViewModel(
    private val repository: WorkoutRepository,
    private val feedbackManager: FeedbackManager
) : ViewModel() {

    private val audioEngine = AudioEngine()
    private val audioRecordHelper = AudioRecordHelper()

    private val _uiState = MutableStateFlow(WorkoutSessionUiState())
    val uiState: StateFlow<WorkoutSessionUiState> = _uiState.asStateFlow()

    private var audioCaptureJob: Job? = null
    private var restTimerJob: Job? = null
    private var setStartTimestamp = 0L

    init {
        loadWorkouts()
        observeAudioEngine()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            repository.allWorkouts.collect { workouts ->
                _uiState.update { current ->
                    current.copy(
                        availableWorkouts = workouts,
                        currentWorkout = current.currentWorkout ?: workouts.firstOrNull()
                    )
                }
                _uiState.value.currentWorkout?.let { loadSetsForWorkout(it.id) }
            }
        }
    }

    fun selectWorkout(workout: WorkoutEntity) {
        _uiState.update {
            it.copy(
                currentWorkout = workout,
                currentSetNumber = 1,
                completedReps = 0,
                restTotalSeconds = workout.defaultRestSeconds,
                restTimeRemainingSeconds = workout.defaultRestSeconds
            )
        }
        loadSetsForWorkout(workout.id)
    }

    private fun loadSetsForWorkout(workoutId: Long) {
        viewModelScope.launch {
            repository.getSetsForWorkout(workoutId).collect { sets ->
                val nextSetNum = (sets.maxOfOrNull { it.setNumber } ?: 0) + 1
                _uiState.update {
                    it.copy(
                        completedSetsHistory = sets,
                        currentSetNumber = nextSetNum
                    )
                }
            }
        }
    }

    private fun observeAudioEngine() {
        viewModelScope.launch {
            audioEngine.state.collect { engineState ->
                _uiState.update { it.copy(engineState = engineState) }
                if (engineState is AudioEngineState.Listening) {
                    _uiState.update {
                        it.copy(
                            noiseFloorDb = engineState.noiseFloorDb,
                            thresholdDb = engineState.thresholdDb
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            audioEngine.events.collect { event ->
                when (event) {
                    is AudioEvent.CountTriggered -> {
                        _uiState.update { it.copy(completedReps = event.repCount) }
                        feedbackManager.onRepCounted(event.repCount)
                    }
                    is AudioEvent.RmsUpdated -> {
                        _uiState.update { it.copy(currentDb = event.currentDb) }
                    }
                    else -> Unit
                }
            }
        }
    }

    fun startSet() {
        stopRestTimer()
        audioEngine.startSession()
        setStartTimestamp = System.currentTimeMillis()

        _uiState.update {
            it.copy(
                isSessionActive = true,
                isResting = false,
                completedReps = 0
            )
        }

        audioCaptureJob?.cancel()
        audioCaptureJob = viewModelScope.launch {
            audioRecordHelper.startCapture().collect { buffer ->
                audioEngine.processBuffer(buffer)
            }
        }
    }

    fun completeSet() {
        audioCaptureJob?.cancel()
        audioRecordHelper.stopCapture()
        audioEngine.stopSession()

        val currentState = _uiState.value
        val workoutId = currentState.currentWorkout?.id ?: return
        val durationMillis = System.currentTimeMillis() - setStartTimestamp

        val setEntity = SetEntity(
            workoutId = workoutId,
            setNumber = currentState.currentSetNumber,
            weight = currentState.weight,
            targetReps = currentState.targetReps,
            completedReps = currentState.completedReps,
            durationMillis = durationMillis,
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.insertSet(setEntity)
        }

        feedbackManager.onSetCompleted(currentState.currentSetNumber, currentState.completedReps)

        _uiState.update {
            it.copy(
                isSessionActive = false,
                isResting = true,
                currentSetNumber = it.currentSetNumber + 1,
                restTotalSeconds = it.currentWorkout?.defaultRestSeconds ?: 60,
                restTimeRemainingSeconds = it.currentWorkout?.defaultRestSeconds ?: 60
            )
        }

        startRestTimer()
    }

    private fun startRestTimer() {
        stopRestTimer()
        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restTimeRemainingSeconds > 0) {
                delay(1000L)
                _uiState.update { it.copy(restTimeRemainingSeconds = it.restTimeRemainingSeconds - 1) }
                if (_uiState.value.restTimeRemainingSeconds == 5) {
                    feedbackManager.onRestEndingCountdown()
                }
            }
            feedbackManager.speak("휴식 종료! 다음 세트를 시작하세요.")
            _uiState.update { it.copy(isResting = false) }
        }
    }

    fun skipRest() {
        stopRestTimer()
        _uiState.update { it.copy(isResting = false, restTimeRemainingSeconds = 0) }
    }

    private fun stopRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = null
    }

    fun manualAdjustCount(delta: Int) {
        val newCount = (_uiState.value.completedReps + delta).coerceAtLeast(0)
        audioEngine.manualAdjustCount(delta)
        _uiState.update { it.copy(completedReps = newCount) }
    }

    fun adjustWeight(delta: Float) {
        _uiState.update { it.copy(weight = (it.weight + delta).coerceAtLeast(0.0f)) }
    }

    fun adjustTargetReps(delta: Int) {
        _uiState.update { it.copy(targetReps = (it.targetReps + delta).coerceAtLeast(1)) }
    }

    override fun onCleared() {
        super.onCleared()
        audioCaptureJob?.cancel()
        audioRecordHelper.stopCapture()
        audioEngine.stopSession()
        stopRestTimer()
        feedbackManager.release()
    }
}
