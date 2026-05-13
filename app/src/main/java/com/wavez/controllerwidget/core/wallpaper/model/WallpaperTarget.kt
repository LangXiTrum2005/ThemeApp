package com.wavez.controllerwidget.core.wallpaper.model

import android.app.WallpaperManager

enum class WallpaperTarget(val flag: Int) {
    HOME(WallpaperManager.FLAG_SYSTEM),
    LOCK(WallpaperManager.FLAG_LOCK),
    BOTH(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
}
