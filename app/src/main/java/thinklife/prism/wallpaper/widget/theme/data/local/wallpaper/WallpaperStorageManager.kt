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

import thinklife.prism.wallpaper.widget.theme.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperStorageManager @Inject constructor(
    private val pathProvider: WallpaperFilePathProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun isWallpaperDownloaded(wallpaperId: String): Boolean = withContext(ioDispatcher) {
        pathProvider.getWallpaperFile(wallpaperId).isFile
    }

    suspend fun getWallpaperFile(wallpaperId: String): File = withContext(ioDispatcher) {
        pathProvider.getWallpaperFile(wallpaperId)
    }

    // Atomically renames the temp file to the final destination.
    // Caller (WallpaperDownloader) is responsible for writing the temp file first.
    suspend fun moveTempToFinal(wallpaperId: String): File = withContext(ioDispatcher) {
        val temp = pathProvider.getTempWallpaperFile(wallpaperId)
        val target = pathProvider.getWallpaperFile(wallpaperId)
        target.parentFile?.mkdirs()

        if (!temp.isFile) {
            error("Temp wallpaper file does not exist: ${temp.absolutePath}")
        }
        if (temp.length() == 0L) {
            temp.delete()
            error("Downloaded wallpaper file is empty: ${temp.absolutePath}")
        }
        if (target.exists() && !target.delete()) {
            error("Cannot replace existing wallpaper file: ${target.absolutePath}")
        }
        if (!temp.renameTo(target)) {
            error("Cannot move temp wallpaper file to: ${target.absolutePath}")
        }
        target
    }

    suspend fun deleteWallpaper(wallpaperId: String) = withContext(ioDispatcher) {
        pathProvider.getWallpaperFile(wallpaperId).delete()
        pathProvider.getTempWallpaperFile(wallpaperId).delete()
    }
}
