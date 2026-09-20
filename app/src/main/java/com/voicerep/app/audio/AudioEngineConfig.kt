package com.voicerep.app.audio

data class AudioEngineConfig(
    val sampleRate: Int = AudioConfig.SAMPLE_RATE,
    val frameDurationMs: Long = AudioConfig.FRAME_DURATION_MS.toLong(),
    val calibrationTimeMs: Long = AudioConfig.DEFAULT_CALIBRATION_TIME_MS,
    val triggerMarginDb: Double = AudioConfig.DEFAULT_TRIGGER_MARGIN_DB,
    val minSustainedMs: Long = AudioConfig.DEFAULT_MIN_SUSTAINED_MS,
    val maxSustainedMs: Long = AudioConfig.DEFAULT_MAX_SUSTAINED_MS,
    val minRepIntervalMs: Long = AudioConfig.DEFAULT_MIN_REP_INTERVAL_MS,
    val minPeakDb: Double = 40.0 // 최소 절대 음량 기준 (너무 조용한 곳에서 속삭임/바스락 방지)
)
