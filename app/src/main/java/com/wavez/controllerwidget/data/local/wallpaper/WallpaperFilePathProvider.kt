package com.wavez.controllerwidget.data.local.wallpaper

import android.content.Context
import com.wavez.controllerwidget.data.local.WallpaperStorageConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperFilePathProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getWallpaperFile(wallpaperId: String): File {
        return File(imagesDir(), "${sanitize(wallpaperId)}.${WallpaperStorageConfig.DEFAULT_WALLPAPER_EXTENSION}")
    }

    fun getTempWallpaperFile(wallpaperId: String): File {
        return File(
            imagesDir(),
            "${sanitize(wallpaperId)}.${WallpaperStorageConfig.DEFAULT_WALLPAPER_EXTENSION}${WallpaperStorageConfig.WALLPAPER_TEMP_SUFFIX}"
        )
    }

    private fun imagesDir(): File {
        return File(wallpaperBaseDir(), WallpaperStorageConfig.WALLPAPER_IMAGES_DIR)
    }

    private fun wallpaperBaseDir(): File {
        return File(context.filesDir, WallpaperStorageConfig.WALLPAPER_BASE_DIR)
    }

    private fun sanitize(value: String): String {
        return value.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }
}
