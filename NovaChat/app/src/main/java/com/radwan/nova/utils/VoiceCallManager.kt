package com.radwan.nova.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object VoiceCallManager {
    private const val TAG = "VoiceCallManager"
    private const val SAMPLE_RATE = 16000
    private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
    private const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
    private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private const val BUFFER_SIZE = 1024

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isRecording = false
    private var isPlaying = false
    private var callJob: Job? = null
    private var socket: DatagramSocket? = null

    var isMuted = false
    var isSpeakerOn = false

    @SuppressLint("MissingPermission")
    fun startCall(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = isSpeakerOn

        val minRecBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, AUDIO_FORMAT)
        val minTrackBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT, AUDIO_FORMAT)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                CHANNEL_IN,
                AUDIO_FORMAT,
                maxOf(minRecBuf, BUFFER_SIZE * 4)
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_OUT)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minTrackBuf, BUFFER_SIZE * 4))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioRecord?.startRecording()
            audioTrack?.play()
            isRecording = true
            isPlaying = true

            Log.d(TAG, "Audio hardware initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice call audio", e)
        }
    }

    fun toggleMute(muted: Boolean) {
        isMuted = muted
    }

    fun toggleSpeaker(context: Context, speaker: Boolean) {
        isSpeakerOn = speaker
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = speaker
    }

    fun stopCall(context: Context) {
        isRecording = false
        isPlaying = false
        callJob?.cancel()
        callJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            Log.d(TAG, "Voice call stopped and audio reset")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio", e)
        }
    }
}
