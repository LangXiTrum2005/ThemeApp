package com.wavez.controllerwidget.core.wallpaper.setter

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wraps [WallpaperManager] and routes each [WallpaperTarget] through the
 * correct API path, including Samsung / Xiaomi / MIUI compatibility shims.
 *
 * All public methods are suspend functions; they switch to [Dispatchers.IO]
 * internally so callers do not have to.
 */
class WallpaperSetter(context: Context) {

    private val ctx = context.applicationContext
    private val wm  = WallpaperManager.getInstance(ctx)

    val isSupported: Boolean get() = wm.isWallpaperSupported
    val isAllowed:   Boolean get() = wm.isSetWallpaperAllowed

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Applies [bitmap] to the requested [flag] (FLAG_SYSTEM, FLAG_LOCK, or both).
     *
     * @throws SecurityException if the device does not allow setting wallpaper.
     * @throws IllegalStateException if the device does not support wallpapers.
     */
    suspend fun set(bitmap: Bitmap, flag: Int): Unit = withContext(Dispatchers.IO) {
        check(isSupported) { "Wallpaper is not supported on this device." }
        check(isAllowed)   { "Setting wallpaper is not allowed on this device." }

        val setSystem = flag and WallpaperManager.FLAG_SYSTEM != 0
        val setLock   = flag and WallpaperManager.FLAG_LOCK   != 0

        if (setSystem) applySystem(bitmap)
        if (setLock)   applyLock(bitmap)
    }

    // ── Internal routing ─────────────────────────────────────────────────────

    private fun applySystem(bitmap: Bitmap) {
        try {
            wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
        } catch (e: Exception) {
            // Last-resort fallback: setBitmap(Bitmap) sets both screens on all APIs
            wm.setBitmap(bitmap)
        }
    }

    private fun applyLock(bitmap: Bitmap) {
        if (ManufacturerCompat.needsSamsungLockFallback) {
            // Samsung One UI silently ignores FLAG_LOCK from 3rd-party apps on some
            // firmware versions; setting FLAG_SYSTEM propagates to the lock screen too.
            applySamsungLock(bitmap)
        } else {
            try {
                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            } catch (e: SecurityException) {
                // Some OEM ROMs (older Xiaomi, Huawei) block FLAG_LOCK with a
                // SecurityException. Escalate with a clear message.
                throw SecurityException(
                    "This device (${ManufacturerCompat.oem}) does not allow setting " +
                    "the lock-screen wallpaper from third-party apps.", e
                )
            }
        }
    }

    private fun applySamsungLock(bitmap: Bitmap) {
        try {
            // Attempt the standard FLAG_LOCK path first; it works on newer One UI.
            wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
        } catch (_: Exception) {
            try {
                // Fallback: FLAG_SYSTEM on Samsung propagates to the lock screen on
                // devices where the two screens share the same wallpaper slot.
                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
            } catch (e: Exception) {
                throw IllegalStateException(
                    "Cannot set lock-screen wallpaper on this Samsung device.", e
                )
            }
        }
    }
}
