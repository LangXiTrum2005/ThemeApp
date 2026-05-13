package com.wavez.controllerwidget.core.controllercenter.controllers

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToInt

enum class RingerMode { NORMAL, VIBRATE, SILENT }

class VolumeController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    var onVolumeChanged: ((Int) -> Unit)? = null

    // ContentObserver is reliable across all Android versions (no RECEIVER_EXPORTED issue on API 34+)
    private val volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            onVolumeChanged?.invoke(getCurrentVolume())
        }
    }

    init {
        try {
            context.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                volumeObserver
            )
        } catch (e: Exception) {
            Log.e("ControlCenter", "VolumeController: init failed", e)
        }
    }

    fun setMediaVolume(progress: Int) {
        try {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                (progress / 100f * max).roundToInt().coerceIn(0, max),
                AudioManager.FLAG_SHOW_UI
            )
        } catch (e: Exception) {
            Log.e("ControlCenter", "VolumeController: setMediaVolume failed", e)
        }
    }

    fun setRingerMode(mode: RingerMode) {
        try {
            audioManager.ringerMode = when (mode) {
                RingerMode.NORMAL -> AudioManager.RINGER_MODE_NORMAL
                RingerMode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                RingerMode.SILENT -> AudioManager.RINGER_MODE_SILENT
            }
        } catch (e: Exception) {
            Log.e("ControlCenter", "VolumeController: setRingerMode failed", e)
        }
    }

    fun getRingerMode(): RingerMode = try {
        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_VIBRATE -> RingerMode.VIBRATE
            AudioManager.RINGER_MODE_SILENT -> RingerMode.SILENT
            else -> RingerMode.NORMAL
        }
    } catch (e: Exception) {
        Log.e("ControlCenter", "VolumeController: getRingerMode failed", e)
        RingerMode.NORMAL
    }

    fun getCurrentVolume(): Int = try {
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (max == 0) 0 else (cur * 100f / max).roundToInt()
    } catch (e: Exception) {
        Log.e("ControlCenter", "VolumeController: getCurrentVolume failed", e); 0
    }

    fun release() {
        try { context.contentResolver.unregisterContentObserver(volumeObserver) }
        catch (e: Exception) { Log.e("ControlCenter", "VolumeController: release failed", e) }
    }
}
