package com.wavez.controllerwidget.core.wallpaper.controller

import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

/**
 * Abstract base class for video live wallpapers.
 *
 * Subclass this, implement [provideVideoUri], and register the subclass in
 * AndroidManifest.xml:
 *
 * ```xml
 * <service
 *     android:name=".YourVideoWallpaperService"
 *     android:label="@string/video_wallpaper_label"
 *     android:permission="android.permission.BIND_WALLPAPER"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.service.wallpaper.WallpaperService" />
 *     </intent-filter>
 *     <meta-data
 *         android:name="android.service.wallpaper"
 *         android:resource="@xml/your_wallpaper_description" />
 * </service>
 * ```
 *
 * The simplest implementation:
 * ```kotlin
 * class MyVideoWallpaperService : VideoWallpaperService() {
 *     override fun provideVideoUri(): Uri? =
 *         VideoWallpaperController(this).getSavedVideoUri()
 * }
 * ```
 *
 * Notes:
 * - Audio is silenced automatically (wallpapers must not produce sound).
 * - Playback is paused/resumed as the wallpaper becomes invisible/visible.
 * - [MediaPlayer] is used to avoid adding an ExoPlayer dependency to the core module.
 *   Swap in ExoPlayer in your subclass if you need better codec support.
 */
abstract class VideoWallpaperService : WallpaperService() {

    final override fun onCreateEngine(): Engine = VideoEngine()

    /** Return the video [Uri] that should play as the wallpaper background. */
    abstract fun provideVideoUri(): Uri?

    // ── Engine ────────────────────────────────────────────────────────────────

    inner class VideoEngine : Engine() {

        private var player: MediaPlayer? = null
        private var surfaceReady = false
        private var engineVisible = false

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            setTouchEventsEnabled(false)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceReady = true
            startPlayer(holder)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            surfaceReady = false
            releasePlayer()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            engineVisible = visible
            if (visible) player?.start() else player?.pause()
        }

        override fun onDestroy() {
            super.onDestroy()
            releasePlayer()
        }

        // ── MediaPlayer lifecycle ─────────────────────────────────────────────

        private fun startPlayer(holder: SurfaceHolder) {
            val uri = provideVideoUri() ?: return
            releasePlayer()

            player = MediaPlayer().apply {
                try {
                    setDataSource(applicationContext, uri)
                    setSurface(holder.surface)
                    isLooping = true
                    setVolume(0f, 0f)   // Wallpapers must not produce audio
                    setOnPreparedListener { mp ->
                        if (engineVisible) mp.start()
                    }
                    setOnErrorListener { _, _, _ ->
                        releasePlayer()
                        false           // Do not invoke onCompletion
                    }
                    prepareAsync()
                } catch (_: Exception) {
                    releasePlayer()
                }
            }
        }

        private fun releasePlayer() {
            player?.let {
                if (it.isPlaying) it.stop()
                it.setSurface(null)
                it.release()
            }
            player = null
        }
    }
}
