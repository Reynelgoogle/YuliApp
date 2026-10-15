package com.example.ui.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecordManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingPath: String? = null
    private var isRecordingState = false

    fun isRecording(): Boolean = isRecordingState

    private fun getOutputFileForId(id: String): File {
        val dir = File(context.filesDir, "yuliapp_recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "rec_$id.3gp")
    }

    fun startRecording(id: String): String? {
        stopPlayback()
        val file = getOutputFileForId(id)
        if (file.exists()) {
            file.delete()
        }

        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            recorder.setOutputFile(file.absolutePath)

            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            currentRecordingPath = file.absolutePath
            isRecordingState = true
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            isRecordingState = false
            mediaRecorder?.release()
            mediaRecorder = null
            return null
        }
    }

    fun stopRecording(): String? {
        if (!isRecordingState) return null
        return try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecordingState = false
            currentRecordingPath
        } catch (e: Exception) {
            e.printStackTrace()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecordingState = false
            null
        }
    }

    fun playRecordedFile(filePath: String, onFinished: () -> Unit = {}) {
        stopPlayback()
        val file = File(filePath)
        if (!file.exists()) {
            onFinished()
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    if (mediaPlayer == this) {
                        mediaPlayer = null
                    }
                    onFinished()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFinished()
        }
    }

    fun playUrl(url: String, onFinished: () -> Unit = {}, onError: () -> Unit = {}) {
        stopPlayback()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener {
                    start()
                }
                setOnCompletionListener {
                    it.release()
                    if (mediaPlayer == this) {
                        mediaPlayer = null
                    }
                    onFinished()
                }
                setOnErrorListener { mp, what, extra ->
                    mp.release()
                    if (mediaPlayer == this) {
                        mediaPlayer = null
                    }
                    onError()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError()
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Already released or stopped
        } finally {
            mediaPlayer = null
        }
    }

    fun saveImportedAudio(uri: android.net.Uri, id: String): String? {
        val dir = File(context.filesDir, "yuliapp_recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        // Unique name to avoid overlapping between different imports
        val file = File(dir, "imported_${id}_${System.currentTimeMillis()}.mp3")
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveImportedPhoto(uri: android.net.Uri, id: String): String? {
        val dir = File(context.filesDir, "yuliapp_photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "photo_${id}_${System.currentTimeMillis()}.jpg")
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveImportedVideo(uri: android.net.Uri): String? {
        val dir = File(context.filesDir, "yuliapp_videos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "bg_video_${System.currentTimeMillis()}.mp4")
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
