package com.wavez.themewallpaper.core.wallpaper.model

import android.content.ComponentName
import android.net.Uri

sealed class WallpaperSource {
    data class StaticImage(val uri: Uri) : WallpaperSource()
    data class Gif(val uri: Uri) : WallpaperSource()
    data class Video(val uri: Uri) : WallpaperSource()
    data class Live(val componentName: ComponentName) : WallpaperSource()
}
