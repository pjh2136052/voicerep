package com.voicerep.app.audio

import android.media.AudioFormat
import android.media.MediaRecorder

object AudioConfig {
    const val SAMPLE_RATE = 16000 // 16 kHz
    const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC

    // 40ms 단위 버퍼 (16,000 * 0.04 = 640 samples)
    const val FRAME_DURATION_MS = 40
    const val BUFFER_SIZE_SAMPLES = (SAMPLE_RATE * FRAME_DURATION_MS) / 1000

    // 알고리즘 기본 파라미터
    const val DEFAULT_CALIBRATION_TIME_MS = 2000L
    const val DEFAULT_TRIGGER_MARGIN_DB = 14.0
    const val DEFAULT_MIN_SUSTAINED_MS = 100L
    const val DEFAULT_MAX_SUSTAINED_MS = 700L
    const val DEFAULT_MIN_REP_INTERVAL_MS = 1200L
}
