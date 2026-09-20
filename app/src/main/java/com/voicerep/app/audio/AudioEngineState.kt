package com.voicerep.app.audio

sealed interface AudioEngineState {
    object Idle : AudioEngineState
    data class Calibrating(val progressPercent: Int) : AudioEngineState
    data class Listening(val noiseFloorDb: Double, val thresholdDb: Double) : AudioEngineState
    data class CandidateDetected(val durationMs: Long, val currentRmsDb: Double) : AudioEngineState
    data class Cooldown(val remainingMs: Long) : AudioEngineState
    object Paused : AudioEngineState
}

sealed interface AudioEvent {
    data class CountTriggered(
        val repCount: Int,
        val sustainedDurationMs: Long,
        val peakDb: Double,
        val timestamp: Long = System.currentTimeMillis()
    ) : AudioEvent

    data class StateChanged(val state: AudioEngineState) : AudioEvent
    data class RmsUpdated(val currentDb: Double, val peakDb: Double) : AudioEvent
    data class CalibrationFinished(val noiseFloorDb: Double, val thresholdDb: Double) : AudioEvent
}
