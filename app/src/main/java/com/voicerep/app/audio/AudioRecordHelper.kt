package com.voicerep.app.audio

import android.annotation.SuppressLint
import android.media.AudioRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

class AudioRecordHelper {

    private var audioRecord: AudioRecord? = null

    @SuppressLint("MissingPermission")
    fun startCapture(): Flow<ShortArray> = flow {
        val minBufferSize = AudioRecord.getMinBufferSize(
            AudioConfig.SAMPLE_RATE,
            AudioConfig.CHANNEL_CONFIG,
            AudioConfig.AUDIO_FORMAT
        )
        val bufferSize = maxOf(minBufferSize, AudioConfig.BUFFER_SIZE_SAMPLES * 2)

        val record = AudioRecord(
            AudioConfig.AUDIO_SOURCE,
            AudioConfig.SAMPLE_RATE,
            AudioConfig.CHANNEL_CONFIG,
            AudioConfig.AUDIO_FORMAT,
            bufferSize
        )
        audioRecord = record

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return@flow
        }

        record.startRecording()
        val buffer = ShortArray(AudioConfig.BUFFER_SIZE_SAMPLES)

        try {
            while (currentCoroutineContext().isActive && record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val readSize = record.read(buffer, 0, buffer.size)
                if (readSize > 0) {
                    emit(buffer.copyOf(readSize))
                }
            }
        } finally {
            stopCapture()
        }
    }.flowOn(Dispatchers.IO)

    fun stopCapture() {
        try {
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioRecord = null
        }
    }
}
