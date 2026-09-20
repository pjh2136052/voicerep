package com.voicerep.app.audio

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioEngine(
    val config: AudioEngineConfig = AudioEngineConfig()
) {
    private val _state = MutableStateFlow<AudioEngineState>(AudioEngineState.Idle)
    val state: StateFlow<AudioEngineState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AudioEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AudioEvent> = _events.asSharedFlow()

    private var currentCount = 0
    private var noiseFloorDb = 35.0
    private var thresholdDb = 35.0 + config.triggerMarginDb

    // 캘리브레이션 관련
    private var calibrationStartTimestamp = 0L
    private val calibrationDbs = mutableListOf<Double>()

    // 발성 후보 측정 관련
    private var voiceStartTimestamp = 0L
    private var peakDbInCandidate = 0.0

    // 디바운스 쿨다운 관련
    private var lastCountTimestamp = 0L

    fun startSession() {
        currentCount = 0
        calibrationStartTimestamp = 0L
        calibrationDbs.clear()
        voiceStartTimestamp = 0L
        peakDbInCandidate = 0.0
        lastCountTimestamp = 0L
        _state.value = AudioEngineState.Calibrating(progressPercent = 0)
    }

    fun pauseSession() {
        _state.value = AudioEngineState.Paused
    }

    fun resumeSession() {
        _state.value = AudioEngineState.Listening(noiseFloorDb, thresholdDb)
    }

    fun stopSession() {
        _state.value = AudioEngineState.Idle
        voiceStartTimestamp = 0L
    }

    fun resetCount() {
        currentCount = 0
    }

    fun manualAdjustCount(delta: Int) {
        currentCount = (currentCount + delta).coerceAtLeast(0)
        _events.tryEmit(
            AudioEvent.CountTriggered(
                repCount = currentCount,
                sustainedDurationMs = 0L,
                peakDb = 0.0
            )
        )
    }

    /**
     * 단위 테스트 및 실시간 캡처에서 공통으로 호출 가능한 단일 PCM 버퍼 프레임 처리기
     */
    fun processBuffer(buffer: ShortArray, currentTimestamp: Long = System.currentTimeMillis()): AudioEvent? {
        val displayDb = AudioRmsCalculator.calculateDisplayDb(buffer, buffer.size)
        _events.tryEmit(AudioEvent.RmsUpdated(currentDb = displayDb, peakDb = peakDbInCandidate))

        return when (val currentState = _state.value) {
            is AudioEngineState.Idle, is AudioEngineState.Paused -> {
                null
            }

            is AudioEngineState.Calibrating -> {
                processCalibration(displayDb, currentTimestamp)
            }

            is AudioEngineState.Cooldown -> {
                val elapsedSinceCount = currentTimestamp - lastCountTimestamp
                val remaining = config.minRepIntervalMs - elapsedSinceCount
                if (remaining <= 0) {
                    _state.value = AudioEngineState.Listening(noiseFloorDb, thresholdDb)
                } else {
                    _state.value = AudioEngineState.Cooldown(remainingMs = remaining)
                }
                null
            }

            is AudioEngineState.Listening, is AudioEngineState.CandidateDetected -> {
                processListeningAndDetection(displayDb, currentTimestamp)
            }
        }
    }

    private fun processCalibration(currentDb: Double, currentTimestamp: Long): AudioEvent? {
        if (calibrationStartTimestamp == 0L) {
            calibrationStartTimestamp = currentTimestamp
        }

        calibrationDbs.add(currentDb)
        val elapsed = currentTimestamp - calibrationStartTimestamp
        val progress = ((elapsed.toDouble() / config.calibrationTimeMs) * 100).toInt().coerceIn(0, 100)
        _state.value = AudioEngineState.Calibrating(progressPercent = progress)

        if (elapsed >= config.calibrationTimeMs) {
            // 이상치(상위 10% 급격한 충돌음) 제거 후 중앙 80% 평균치 계산
            val sorted = calibrationDbs.sorted()
            val trimmed = if (sorted.size > 10) {
                sorted.subList((sorted.size * 0.1).toInt(), (sorted.size * 0.9).toInt())
            } else sorted
            noiseFloorDb = if (trimmed.isNotEmpty()) trimmed.average() else 35.0
            thresholdDb = maxOf(noiseFloorDb + config.triggerMarginDb, config.minPeakDb)

            _state.value = AudioEngineState.Listening(noiseFloorDb, thresholdDb)
            val event = AudioEvent.CalibrationFinished(noiseFloorDb, thresholdDb)
            _events.tryEmit(event)
            return event
        }
        return null
    }

    private fun processListeningAndDetection(currentDb: Double, currentTimestamp: Long): AudioEvent? {
        // 디바운스 체크 (안전장치)
        if (currentTimestamp - lastCountTimestamp < config.minRepIntervalMs) {
            return null
        }

        val isOverThreshold = currentDb >= thresholdDb && currentDb >= config.minPeakDb

        if (isOverThreshold) {
            if (voiceStartTimestamp == 0L) {
                voiceStartTimestamp = currentTimestamp
                peakDbInCandidate = currentDb
            } else {
                peakDbInCandidate = maxOf(peakDbInCandidate, currentDb)
            }
            val duration = currentTimestamp - voiceStartTimestamp
            _state.value = AudioEngineState.CandidateDetected(durationMs = duration, currentRmsDb = currentDb)

            // 최대 발성 지속 시간(예: 700ms) 초과 시 배경음/음악으로 간주하여 기각
            if (duration > config.maxSustainedMs) {
                voiceStartTimestamp = 0L
                _state.value = AudioEngineState.Listening(noiseFloorDb, thresholdDb)
            }
            return null
        } else {
            // 소리가 임계치 아래로 내려왔을 때 이전 발성의 지속 시간 검증
            if (voiceStartTimestamp != 0L) {
                val duration = currentTimestamp - voiceStartTimestamp
                val recordedPeak = peakDbInCandidate
                voiceStartTimestamp = 0L
                peakDbInCandidate = 0.0

                if (duration in config.minSustainedMs..config.maxSustainedMs) {
                    // 유효 발성 판정: 카운트 증가 & 디바운스 쿨다운 진입
                    currentCount++
                    lastCountTimestamp = currentTimestamp
                    _state.value = AudioEngineState.Cooldown(remainingMs = config.minRepIntervalMs)

                    val event = AudioEvent.CountTriggered(
                        repCount = currentCount,
                        sustainedDurationMs = duration,
                        peakDb = recordedPeak,
                        timestamp = currentTimestamp
                    )
                    _events.tryEmit(event)
                    return event
                } else {
                    // 100ms 미만의 덤벨 충돌, 박수 등의 단발성 소음 배제
                    _state.value = AudioEngineState.Listening(noiseFloorDb, thresholdDb)
                }
            }
            return null
        }
    }
}
