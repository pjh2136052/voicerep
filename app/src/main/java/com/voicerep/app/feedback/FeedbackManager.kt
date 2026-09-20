package com.voicerep.app.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import java.util.Locale

class FeedbackManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var toneGenerator: ToneGenerator? = null

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        tts = TextToSpeech(context.applicationContext, this)
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.KOREAN)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                tts?.setSpeechRate(1.2f) // 운동 템포에 맞춰 약간 빠른 재생
            }
        }
    }

    /**
     * 카운트 증가 시 짧은 비프음 및 진동
     */
    fun onRepCounted(rep: Int) {
        vibrate(shortDurationMs = 70L)
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)

        // 5회, 10회 등 특정 구간 또는 매 카운트마다 TTS 음성 지원 가능
        if (isTtsReady && (rep % 5 == 0)) {
            speak("$rep 회")
        }
    }

    /**
     * 세트 완료 시 팡파레 톤 및 음성 안내
     */
    fun onSetCompleted(setNumber: Int, completedReps: Int) {
        vibrate(shortDurationMs = 250L)
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
        speak("${setNumber}세트 ${completedReps}회 완료. 휴식하세요.")
    }

    /**
     * 휴식 종료 5초 전 경고음
     */
    fun onRestEndingCountdown() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
    }

    fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VoiceRep_TTS")
        }
    }

    private fun vibrate(shortDurationMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(shortDurationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(shortDurationMs)
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            toneGenerator?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
