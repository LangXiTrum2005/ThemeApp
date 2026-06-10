package com.wavez.themewallpaper.core.wallpaper.setter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build

/**
 * Detects OEM-specific quirks that affect wallpaper-setting behaviour.
 *
 * Samsung One UI and Xiaomi MIUI both impose restrictions on top of the standard
 * [android.app.WallpaperManager] APIs. Rather than guessing at runtime, we read
 * system properties and package info once (lazily) and expose simple boolean flags
 * that [WallpaperSetter] uses to choose the right code path.
 */
internal object ManufacturerCompat {

    enum class Oem { SAMSUNG, XIAOMI, HUAWEI, OTHER }

    val oem: Oem by lazy {
        when (Build.MANUFACTURER.lowercase()) {
            "samsung"              -> Oem.SAMSUNG
            "xiaomi", "redmi"     -> Oem.XIAOMI
            "huawei", "honor"     -> Oem.HUAWEI
            else                  -> Oem.OTHER
        }
    }

    // ── MIUI detection ───────────────────────────────────────────────────────

    val isMiui: Boolean by lazy {
        readSystemProperty("ro.miui.ui.version.name").isNotEmpty()
    }

    val miuiVersion: Int by lazy {
        readSystemProperty("ro.miui.ui.version.code").toIntOrNull() ?: 0
    }

    /**
     * MIUI 12+ tightens background task restrictions. Wallpaper writes from a
     * plain coroutine on the IO dispatcher may be silently dropped.
     * Callers should use a foreground service when this returns true.
     */
    val requiresForegroundOnMiui: Boolean
        get() = isMiui && miuiVersion >= 12

    // ── Samsung One UI detection ─────────────────────────────────────────────

    /**
     * Samsung One UI ≥ Android 9 (API 28) rejects FLAG_LOCK writes issued from
     * outside its own "WallpaperManagerService" wrapper. We fall back to the
     * public FLAG_SYSTEM path on those devices.
     */
    val needsSamsungLockFallback: Boolean
        get() = oem == Oem.SAMSUNG

    fun isSamsungOneUI(context: Context): Boolean = try {
        context.packageManager.getPackageInfo("com.samsung.android.oneui", 0)
        true
    } catch (_: Exception) {
        false
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    @SuppressLint("PrivateApi")
    private fun readSystemProperty(key: String): String = try {
        val cls = Class.forName("android.os.SystemProperties")
        cls.getMethod("get", String::class.java).invoke(null, key) as? String ?: ""
    } catch (_: Exception) {
        ""
    }
}
