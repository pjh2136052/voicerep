package com.voicerep.app.ui.session

import com.voicerep.app.audio.AudioEngineState
import com.voicerep.app.data.local.entity.SetEntity
import com.voicerep.app.data.local.entity.WorkoutEntity

data class WorkoutSessionUiState(
    val currentWorkout: WorkoutEntity? = null,
    val availableWorkouts: List<WorkoutEntity> = emptyList(),
    val currentSetNumber: Int = 1,
    val weight: Float = 60.0f,
    val targetReps: Int = 10,
    val completedReps: Int = 0,
    val isSessionActive: Boolean = false,
    val isResting: Boolean = false,
    val restTimeRemainingSeconds: Int = 60,
    val restTotalSeconds: Int = 60,
    val currentDb: Double = 0.0,
    val thresholdDb: Double = 50.0,
    val noiseFloorDb: Double = 35.0,
    val engineState: AudioEngineState = AudioEngineState.Idle,
    val completedSetsHistory: List<SetEntity> = emptyList()
)
