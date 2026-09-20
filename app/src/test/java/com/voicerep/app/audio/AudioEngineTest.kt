package com.voicerep.app.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.sin

class AudioEngineTest {

    private lateinit var engine: AudioEngine
    private val config = AudioEngineConfig(
        calibrationTimeMs = 1000L,
        triggerMarginDb = 15.0,
        minSustainedMs = 100L,
        maxSustainedMs = 700L,
        minRepIntervalMs = 1200L,
        minPeakDb = 40.0
    )

    @Before
    fun setUp() {
        engine = AudioEngine(config)
    }

    private fun generateBuffer(amplitude: Double, frequencyHz: Double = 440.0): ShortArray {
        val size = AudioConfig.BUFFER_SIZE_SAMPLES
        val buffer = ShortArray(size)
        val angularFreq = 2.0 * Math.PI * frequencyHz / AudioConfig.SAMPLE_RATE
        for (i in 0 until size) {
            val sample = (amplitude * sin(angularFreq * i)).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
            buffer[i] = sample.toInt().toShort()
        }
        return buffer
    }

    private fun runCalibration(virtualTime: Long): Long {
        engine.startSession()
        var time = virtualTime
        val lowNoiseBuffer = generateBuffer(amplitude = 50.0) // 조용한 배경 소음 (약 30~35 dB)
        val frames = (config.calibrationTimeMs / AudioConfig.FRAME_DURATION_MS).toInt() + 1

        for (i in 0 until frames) {
            time += AudioConfig.FRAME_DURATION_MS
            engine.processBuffer(lowNoiseBuffer, time)
        }
        return time
    }

    @Test
    fun test_calibration_transitions_to_listening() {
        var time = 10000L
        time = runCalibration(time)

        assertTrue(
            "캘리브레이션 후 Listening 상태여야 함",
            engine.state.value is AudioEngineState.Listening
        )
    }

    @Test
    fun test_short_noise_spike_is_rejected() {
        // 단발성 덤벨 충돌음 (1개 프레임 = 40ms) 은 최소 지속 시간(100ms) 미만이므로 무시되어야 함
        var time = 10000L
        time = runCalibration(time)

        val loudBuffer = generateBuffer(amplitude = 25000.0) // 매우 큰 소리 (약 80dB)
        val quietBuffer = generateBuffer(amplitude = 30.0)

        // 40ms 스파이크
        time += AudioConfig.FRAME_DURATION_MS
        val event1 = engine.processBuffer(loudBuffer, time)

        // 다시 조용해짐
        time += AudioConfig.FRAME_DURATION_MS
        val event2 = engine.processBuffer(quietBuffer, time)

        assertTrue("40ms 충돌음에서는 CountTriggered가 발생하지 않아야 함", event1 !is AudioEvent.CountTriggered)
        assertTrue("충돌음 종료 시에도 카운트되지 않아야 함", event2 !is AudioEvent.CountTriggered)
    }

    @Test
    fun test_valid_voice_is_counted() {
        // 유효한 기합/발성 (200ms 지속 = 5 프레임)
        var time = 10000L
        time = runCalibration(time)

        val loudBuffer = generateBuffer(amplitude = 20000.0) // 약 75dB
        val quietBuffer = generateBuffer(amplitude = 30.0)

        // 200ms 동안 발성 (5 프레임)
        for (i in 0 until 5) {
            time += AudioConfig.FRAME_DURATION_MS
            engine.processBuffer(loudBuffer, time)
        }

        // 발성 종료 (조용해짐) -> 이 순간 유효 지속시간 검증 완료 후 카운트 방출
        time += AudioConfig.FRAME_DURATION_MS
        val event = engine.processBuffer(quietBuffer, time)

        assertTrue("유효 지속시간 충족 시 CountTriggered 이벤트가 발생해야 함", event is AudioEvent.CountTriggered)
        val countEvent = event as AudioEvent.CountTriggered
        assertEquals(1, countEvent.repCount)
        assertTrue(countEvent.sustainedDurationMs >= 100L)
    }

    @Test
    fun test_rapid_repetitions_are_debounced() {
        // 첫 번째 카운트 후 500ms 만에 다시 발성할 경우, 디바운스 쿨다운(1200ms)에 의해 무시되어야 함
        var time = 10000L
        time = runCalibration(time)

        val loudBuffer = generateBuffer(amplitude = 20000.0)
        val quietBuffer = generateBuffer(amplitude = 30.0)

        // 1회차 발성 (200ms)
        for (i in 0 until 5) {
            time += AudioConfig.FRAME_DURATION_MS
            engine.processBuffer(loudBuffer, time)
        }
        time += AudioConfig.FRAME_DURATION_MS
        val event1 = engine.processBuffer(quietBuffer, time)
        assertTrue("첫 번째 카운트 성공", event1 is AudioEvent.CountTriggered)

        // 300ms 휴식 (총 경과: 약 500ms < 1200ms 쿨다운)
        for (i in 0 until 7) {
            time += AudioConfig.FRAME_DURATION_MS
            engine.processBuffer(quietBuffer, time)
        }

        // 2회차 조기 발성 (200ms)
        for (i in 0 until 5) {
            time += AudioConfig.FRAME_DURATION_MS
            engine.processBuffer(loudBuffer, time)
        }
        time += AudioConfig.FRAME_DURATION_MS
        val event2 = engine.processBuffer(quietBuffer, time)

        assertTrue("디바운스 쿨다운 중에는 두 번째 카운트가 트리거되지 않아야 함", event2 !is AudioEvent.CountTriggered)
    }

    @Test
    fun test_excessive_duration_noise_is_rejected() {
        // 음악이나 긴 대화 (1000ms 지속 > maxSustainedMs 700ms) 는 무시되어야 함
        var time = 10000L
        time = runCalibration(time)

        val loudBuffer = generateBuffer(amplitude = 20000.0)
        val quietBuffer = generateBuffer(amplitude = 30.0)

        // 1000ms 지속 (25 프레임)
        for (i in 0 until 25) {
            time += AudioConfig.FRAME_DURATION_MS
            engine.processBuffer(loudBuffer, time)
        }
        time += AudioConfig.FRAME_DURATION_MS
        val event = engine.processBuffer(quietBuffer, time)

        assertTrue("지속 시간이 너무 긴 소음은 카운트되지 않아야 함", event !is AudioEvent.CountTriggered)
    }
}
