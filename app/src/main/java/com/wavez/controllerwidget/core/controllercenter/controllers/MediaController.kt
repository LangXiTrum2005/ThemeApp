package com.wavez.controllerwidget.core.controllercenter.controllers

import android.content.ComponentName
import android.content.BroadcastReceiver
import android.content.Context
import android.graphics.Bitmap
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadata
import android.media.session.MediaController as AndroidMediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.media.session.PlaybackState
import android.util.Log
import androidx.core.content.ContextCompat
import com.wavez.controllerwidget.core.controllercenter.PermissionHelper
import com.wavez.controllerwidget.core.controllercenter.service.ControlNotificationListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class TrackInfo(
    val title: String,
    val artist: String,
    val albumArt: Bitmap?,
    val duration: Long,
    val position: Long
)

class MediaController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    var onTrackChanged: ((TrackInfo?) -> Unit)? = null
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null

    private var activeController: AndroidMediaController? = null
    private var isSessionListenerRegistered = false

    private val controllerCallback = object : AndroidMediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            onTrackChanged?.invoke(metadata?.toTrackInfo(getPosition()))
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            onPlaybackStateChanged?.invoke(state?.state == PlaybackState.STATE_PLAYING)
        }
    }

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        bindToFirst(controllers)
    }

    private val notificationAccessReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ControlNotificationListener.ACTION_NOTIFICATION_LISTENER_CONNECTED -> refreshSessions()
                ControlNotificationListener.ACTION_NOTIFICATION_LISTENER_DISCONNECTED -> {
                    unregisterSessionListener()
                    clearActiveController()
                }
            }
        }
    }

    init {
        registerNotificationAccessReceiver()
        refreshSessions()
    }

    fun refreshSessions() {
        if (!PermissionHelper.hasNotificationAccess(context)) {
            Log.w(TAG, "MediaController: notification listener access is not granted")
            unregisterSessionListener()
            clearActiveController()
            return
        }

        try {
            val component = ComponentName(context, ControlNotificationListener::class.java)
            if (!isSessionListenerRegistered) {
                sessionManager.addOnActiveSessionsChangedListener(sessionListener, component)
                isSessionListenerRegistered = true
            }
            bindToFirst(sessionManager.getActiveSessions(component))
        } catch (e: SecurityException) {
            Log.w(TAG, "MediaController: missing permission to control media", e)
            unregisterSessionListener()
            clearActiveController()
        } catch (e: Exception) {
            Log.e(TAG, "MediaController: init failed", e)
            clearActiveController()
        }
    }

    private fun bindToFirst(controllers: List<AndroidMediaController>?) {
        runCatching { activeController?.unregisterCallback(controllerCallback) }
        activeController = controllers?.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers?.firstOrNull {
            it.metadata != null && it.playbackState?.state != PlaybackState.STATE_STOPPED
        } ?: controllers?.firstOrNull()
        runCatching { activeController?.registerCallback(controllerCallback) }
        onTrackChanged?.invoke(getCurrentTrack())
        onPlaybackStateChanged?.invoke(isPlaying())
    }

    private fun clearActiveController() {
        runCatching { activeController?.unregisterCallback(controllerCallback) }
        activeController = null
        onTrackChanged?.invoke(null)
        onPlaybackStateChanged?.invoke(false)
    }

    private fun unregisterSessionListener() {
        if (!isSessionListenerRegistered) return
        runCatching { sessionManager.removeOnActiveSessionsChangedListener(sessionListener) }
        isSessionListenerRegistered = false
    }

    private fun registerNotificationAccessReceiver() {
        val filter = IntentFilter().apply {
            addAction(ControlNotificationListener.ACTION_NOTIFICATION_LISTENER_CONNECTED)
            addAction(ControlNotificationListener.ACTION_NOTIFICATION_LISTENER_DISCONNECTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(notificationAccessReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                context,
                notificationAccessReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    fun play() = safeTransport { it.play() }
    fun pause() = safeTransport { it.pause() }
    fun next() = safeTransport { it.skipToNext() }
    fun previous() = safeTransport { it.skipToPrevious() }
    fun seekTo(positionMs: Long) = safeTransport { it.seekTo(positionMs) }

    fun getCurrentTrack(): TrackInfo? =
        activeController?.metadata?.toTrackInfo(getPosition())

    fun isPlaying(): Boolean =
        activeController?.playbackState?.state == PlaybackState.STATE_PLAYING

    private fun getPosition(): Long = activeController?.playbackState?.position ?: 0L

    private fun safeTransport(block: (AndroidMediaController.TransportControls) -> Unit) {
        try {
            if (activeController == null) refreshSessions()
            activeController?.transportControls?.let(block)
        } catch (e: SecurityException) {
            Log.w(TAG, "MediaController: transport action missing permission", e)
            clearActiveController()
        } catch (e: Exception) {
            Log.e(TAG, "MediaController: transport action failed", e)
        }
    }

    private fun MediaMetadata.toTrackInfo(position: Long) = TrackInfo(
        title = getString(MediaMetadata.METADATA_KEY_TITLE) ?: "",
        artist = getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "",
        albumArt = getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART),
        duration = getLong(MediaMetadata.METADATA_KEY_DURATION),
        position = position
    )

    fun release() {
        try {
            activeController?.unregisterCallback(controllerCallback)
            unregisterSessionListener()
        } catch (e: Exception) {
            Log.e(TAG, "MediaController: release failed", e)
        } finally {
            runCatching { context.unregisterReceiver(notificationAccessReceiver) }
            activeController = null
        }
    }

    companion object {
        private const val TAG = "ControlCenter"
    }
}
