package com.radwan.nova.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import java.io.File
import java.io.FileInputStream

object AudioHelper {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var currentPlayingUrl: String? = null
    private var onCompletionCallback: (() -> Unit)? = null

    fun startRecording(context: Context, outputFile: File): Boolean {
        return try {
            stopRecording()
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
            false
        }
    }

    fun stopRecording(): Boolean {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            true
        } catch (e: Exception) {
            e.printStackTrace()
            recorder = null
            false
        }
    }

    fun playAudio(context: Context, audioSource: String, onCompletion: () -> Unit) {
        try {
            stopPlaying()
            currentPlayingUrl = audioSource
            onCompletionCallback = onCompletion

            player = MediaPlayer().apply {
                if (audioSource.startsWith("data:audio") || audioSource.startsWith("base64,")) {
                    val base64Data = audioSource.substringAfter("base64,")
                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val tempFile = File.createTempFile("temp_play_", ".m4a", context.cacheDir)
                    tempFile.writeBytes(decodedBytes)
                    setDataSource(tempFile.absolutePath)
                } else if (audioSource.startsWith("http://") || audioSource.startsWith("https://")) {
                    setDataSource(audioSource)
                } else {
                    setDataSource(audioSource)
                }
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener {
                    stopPlaying()
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    stopPlaying()
                    onCompletion()
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopPlaying()
            onCompletion()
        }
    }

    fun stopPlaying() {
        try {
            player?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            player = null
            currentPlayingUrl = null
            onCompletionCallback?.invoke()
            onCompletionCallback = null
        }
    }

    fun isCurrentlyPlaying(url: String): Boolean {
        return player?.isPlaying == true && currentPlayingUrl == url
    }

    fun fileToBase64(file: File): String {
        val bytes = ByteArray(file.length().toInt())
        val fis = FileInputStream(file)
        fis.read(bytes)
        fis.close()
        return "data:audio/m4a;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
