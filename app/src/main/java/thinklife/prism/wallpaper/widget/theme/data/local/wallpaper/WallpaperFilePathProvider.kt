/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.data.local.wallpaper

import android.content.Context
import thinklife.prism.wallpaper.widget.theme.data.local.WallpaperStorageConfig
import thinklife.prism.wallpaper.widget.theme.data.local.sanitize
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


}
