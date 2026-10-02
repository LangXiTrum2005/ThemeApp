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

package thinklife.prism.wallpaper.widget.theme.data.local.controller

import thinklife.prism.wallpaper.widget.theme.di.IoDispatcher
import thinklife.prism.wallpaper.widget.theme.data.local.ThemeStorageConfig
import thinklife.prism.wallpaper.widget.theme.data.local.unzipSafely
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ControllerStorageManager @Inject constructor(
    private val pathProvider: ControllerFilePathProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getThemeFile(themeId: String): File = withContext(ioDispatcher) {
        pathProvider.getThemeFile(themeId)
    }

    suspend fun getExtractedThemeDir(themeId: String): File = withContext(ioDispatcher) {
        pathProvider.getExtractedThemeDir(themeId)
    }

    suspend fun isThemeDownloaded(themeId: String): Boolean = withContext(ioDispatcher) {
        pathProvider.getThemeFile(themeId).isFile
    }

    suspend fun saveTheme(themeId: String, bytes: ByteArray): File {
        return saveTheme(themeId, ByteArrayInputStream(bytes))
    }

    suspend fun saveTheme(themeId: String, inputStream: InputStream): File = withContext(ioDispatcher) {
        val target = pathProvider.getThemeFile(themeId)
        val temp = pathProvider.getTempThemeFile(themeId)
        target.parentFile?.mkdirs()
        temp.delete()

        try {
            inputStream.use { input ->
                temp.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (target.exists() && !target.delete()) {
                error("Cannot replace existing theme file: ${target.absolutePath}")
            }
            if (!temp.renameTo(target)) {
                error("Cannot move temp theme file to target: ${target.absolutePath}")
            }
            target
        } catch (e: Throwable) {
            temp.delete()
            throw e
        }
    }

    suspend fun deleteTheme(themeId: String) = withContext(ioDispatcher) {
        pathProvider.getThemeFile(themeId).delete()
        pathProvider.getTempThemeFile(themeId).delete()
        pathProvider.getExtractedThemeDir(themeId).deleteRecursively()
    }

    suspend fun prepareThemeForApply(themeId: String): File = withContext(ioDispatcher) {
        val archive = pathProvider.getThemeFile(themeId)
        if (!archive.isFile) {
            error("Theme archive does not exist: ${archive.absolutePath}")
        }

        val targetDir = pathProvider.getExtractedThemeDir(themeId)
        // Fast path: already extracted
        if (targetDir.isDirectory && targetDir.listFiles()?.isNotEmpty() == true) {
            return@withContext targetDir
        }

        // Extract into a sibling temp directory first to avoid leaving a partial extraction
        // if the process is interrupted. Only swap once fully complete.
        val tempDir = File(targetDir.parent, "${targetDir.name}${ThemeStorageConfig.THEME_TEMP_SUFFIX}")
        tempDir.deleteRecursively()
        tempDir.mkdirs()
        try {
            unzipSafely(archive, tempDir)
            targetDir.deleteRecursively()
            if (!tempDir.renameTo(targetDir)) {
                error("Cannot move extracted dir to: ${targetDir.absolutePath}")
            }
            targetDir
        } catch (e: Throwable) {
            tempDir.deleteRecursively()
            throw e
        }
    }

}
