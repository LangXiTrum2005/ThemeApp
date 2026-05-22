package com.wavez.controllerwidget.data.model.wallpaper

enum class WallpaperType { IMAGE, GIF, VIDEO }

val WallpaperModel.wallpaperType: WallpaperType
    get() {
        // Strip query string and fragment before checking extension
        val path = url.substringBefore('?').substringBefore('#').lowercase()
        return when {
            path.endsWith(".mp4") || path.endsWith(".webm") ||
            path.endsWith(".mov") || path.endsWith(".avi") -> WallpaperType.VIDEO
            path.endsWith(".gif") -> WallpaperType.GIF
            else -> WallpaperType.IMAGE
        }
    }
