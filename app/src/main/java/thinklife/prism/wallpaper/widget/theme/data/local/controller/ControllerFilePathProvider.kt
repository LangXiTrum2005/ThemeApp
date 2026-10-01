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

import android.content.Context
import thinklife.prism.wallpaper.widget.theme.data.local.ThemeStorageConfig
import thinklife.prism.wallpaper.widget.theme.data.local.sanitize
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ControllerFilePathProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getThemeFile(themeId: String): File {
        return File(archiveDir(), "${sanitize(themeId)}.${ThemeStorageConfig.DEFAULT_THEME_ARCHIVE_EXTENSION}")
    }

    fun getTempThemeFile(themeId: String): File {
        return File(
            archiveDir(),
            "${sanitize(themeId)}.${ThemeStorageConfig.DEFAULT_THEME_ARCHIVE_EXTENSION}${ThemeStorageConfig.THEME_TEMP_SUFFIX}"
        )
    }

    fun getExtractedThemeDir(themeId: String): File {
        return File(extractedBaseDir(), sanitize(themeId))
    }

    fun getDefaultThemeDir(): File {
        return File(themeBaseDir(), ThemeStorageConfig.DEFAULT_THEME_NAME)
    }

    private fun archiveDir(): File {
        return File(themeBaseDir(), ThemeStorageConfig.THEME_ARCHIVE_DIR)
    }

    private fun extractedBaseDir(): File {
        return File(themeBaseDir(), ThemeStorageConfig.THEME_EXTRACTED_DIR)
    }

    private fun themeBaseDir(): File {
        return File(context.filesDir, ThemeStorageConfig.THEME_BASE_DIR)
    }


}
