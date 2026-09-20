package com.voicerep.app.audio

import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

object AudioRmsCalculator {

    /**
     * 16-bit PCM 버퍼로부터 RMS(Root Mean Square) 값을 계산합니다.
     */
    fun calculateRms(buffer: ShortArray, readSize: Int): Double {
        if (readSize <= 0) return 0.0
        var sumSquares = 0.0
        for (i in 0 until readSize) {
            val sample = buffer[i].toDouble()
            sumSquares += sample * sample
        }
        return sqrt(sumSquares / readSize)
    }

    /**
     * RMS 값을 dBFS(-100 dB ~ 0 dB)로 환산합니다.
     */
    fun calculateDbFs(rms: Double): Double {
        val maxAmp = 32767.0
        val normalized = max(rms / maxAmp, 1e-5)
        return 20.0 * log10(normalized)
    }

    /**
     * 화면 UI 게이지용 0.0 ~ 100.0 dB 정규화 값으로 변환합니다.
     * (-80 dBFS -> 0 dB, 0 dBFS -> 80 dB -> 100 scaled)
     */
    fun calculateDisplayDb(buffer: ShortArray, readSize: Int): Double {
        val rms = calculateRms(buffer, readSize)
        val dbFs = calculateDbFs(rms)
        // -80 dBFS를 최소 바닥으로 잡고 0~100 스케일링
        val clamped = (dbFs + 80.0).coerceIn(0.0, 80.0)
        return (clamped / 80.0) * 100.0
    }
}
