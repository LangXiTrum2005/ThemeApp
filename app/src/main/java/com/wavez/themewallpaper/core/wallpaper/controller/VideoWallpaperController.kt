package com.wavez.themewallpaper.core.wallpaper.controller

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Manages the lifecycle of video-based live wallpapers.
 *
 * Video wallpapers on Android require a running [WallpaperService] that renders
 * MediaPlayer frames onto a [android.view.Surface].  This controller:
 *
 * 1. Persists the chosen video [Uri] in SharedPreferences so that
 *    [VideoWallpaperService] can retrieve it after the process is recreated.
 * 2. Launches the system live-wallpaper preview / apply Intent.
 *
 * Typical flow:
 * ```
 * val ctrl = VideoWallpaperController(context)
 * ctrl.apply(videoUri, MyVideoWallpaperService::class.java)
 * ```
 */
class VideoWallpaperController(context: Context) {

    private val ctx   = context.applicationContext
    private val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Persists [uri] and opens the system live-wallpaper preview screen for
     * [serviceClass]. The user taps "Set wallpaper" to confirm.
     */
    fun apply(uri: Uri, serviceClass: Class<*>) {
        saveUri(uri)
        val component = ComponentName(ctx, serviceClass)
        ctx.startActivity(
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun saveUri(uri: Uri) {
        prefs.edit().putString(KEY_VIDEO_URI, uri.toString()).apply()
    }

    fun getSavedUri(): Uri? =
        prefs.getString(KEY_VIDEO_URI, null)?.let { Uri.parse(it) }

    fun clearSavedUri() {
        prefs.edit().remove(KEY_VIDEO_URI).apply()
    }

    // ── Companion ────────────────────────────────────────────────────────────

    companion object {
        const val PREFS_NAME   = "wavez_video_wallpaper"
        const val KEY_VIDEO_URI = "video_uri"
    }
}
