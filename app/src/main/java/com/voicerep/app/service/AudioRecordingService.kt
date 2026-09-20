package com.voicerep.app.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.voicerep.app.MainActivity
import com.voicerep.app.VoiceRepApplication
import com.voicerep.app.audio.AudioEngine
import com.voicerep.app.audio.AudioEvent
import com.voicerep.app.audio.AudioRecordHelper
import com.voicerep.app.feedback.FeedbackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AudioRecordingService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var audioEngine: AudioEngine
    private lateinit var audioRecordHelper: AudioRecordHelper
    private lateinit var feedbackManager: FeedbackManager

    private var wakeLock: PowerManager.WakeLock? = null
    private var captureJob: Job? = null

    private var currentWorkoutTitle: String = "운동"
    private var currentSetNumber: Int = 1
    private var currentReps: Int = 0

    inner class LocalBinder : Binder() {
        fun getService(): AudioRecordingService = this@AudioRecordingService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        audioEngine = AudioEngine()
        audioRecordHelper = AudioRecordHelper()
        feedbackManager = FeedbackManager(this)

        acquireWakeLock()
        observeEvents()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "VoiceRep:AudioRecordingWakeLock"
        )?.apply {
            acquire(2 * 60 * 60 * 1000L) // 최대 2시간 안전 제한
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        wakeLock = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> {
                currentWorkoutTitle = intent.getStringExtra(EXTRA_WORKOUT_TITLE) ?: "운동"
                currentSetNumber = intent.getIntExtra(EXTRA_SET_NUMBER, 1)
                startForegroundNotification()
                startRecording()
            }
            ACTION_STOP -> {
                stopRecording()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, VoiceRepApplication.CHANNEL_ID)
            .setContentTitle("$currentWorkoutTitle - $currentSetNumber 세트")
            .setContentText("현재 카운트: $currentReps 회 (음성 인식 중)")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun startRecording() {
        audioEngine.startSession()
        captureJob?.cancel()
        captureJob = serviceScope.launch(Dispatchers.IO) {
            audioRecordHelper.startCapture().collect { buffer ->
                audioEngine.processBuffer(buffer)
            }
        }
    }

    private fun stopRecording() {
        captureJob?.cancel()
        captureJob = null
        audioRecordHelper.stopCapture()
        audioEngine.stopSession()
    }

    private fun observeEvents() {
        serviceScope.launch {
            audioEngine.events.collect { event ->
                if (event is AudioEvent.CountTriggered) {
                    currentReps = event.repCount
                    feedbackManager.onRepCounted(currentReps)
                    updateNotification()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        releaseWakeLock()
        feedbackManager.release()
        serviceScope.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_AUDIO_RECORDING"
        const val ACTION_STOP = "ACTION_STOP_AUDIO_RECORDING"
        const val EXTRA_WORKOUT_TITLE = "EXTRA_WORKOUT_TITLE"
        const val EXTRA_SET_NUMBER = "EXTRA_SET_NUMBER"
    }
}
