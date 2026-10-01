package com.wavez.themewallpaper.core.wallpaper.controller

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Helper for managing live (WallpaperService-based) wallpapers.
 *
 * All methods are synchronous; they only interact with the PackageManager or
 * dispatch Intents — no heavy work to suspend.
 */
class LiveWallpaperController(context: Context) {

    private val ctx = context.applicationContext
    private val wm  = WallpaperManager.getInstance(ctx)

    // ── Preview / apply ──────────────────────────────────────────────────────

    /**
     * Opens the system live-wallpaper preview screen for [component].
     * The user must tap "Set wallpaper" to confirm — this does NOT set it directly.
     */
    fun preview(component: ComponentName) {
        ctx.startActivity(
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    /**
     * Opens the system live-wallpaper chooser so the user can pick any
     * installed live wallpaper.
     */
    fun openSystemPicker() {
        ctx.startActivity(
            Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    // ── Query helpers ────────────────────────────────────────────────────────

    /** Returns true if [component] is installed as a service on this device. */
    fun isInstalled(component: ComponentName): Boolean = try {
        ctx.packageManager.getServiceInfo(component, PackageManager.GET_META_DATA)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    /**
     * Returns true if [component] is the currently active live wallpaper on
     * the home screen.
     */
    fun isActive(component: ComponentName): Boolean =
        wm.wallpaperInfo?.component == component

    /** Returns the [ComponentName] of the currently active live wallpaper, or null. */
    fun activeComponent(): ComponentName? = wm.wallpaperInfo?.component
}
