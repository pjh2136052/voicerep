package com.voicerep.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class VoiceRepApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VoiceRep Audio Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "운동 중 백그라운드 음성 카운팅을 유지하는 알림 채널입니다."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "voicerep_audio_channel"
    }
}
