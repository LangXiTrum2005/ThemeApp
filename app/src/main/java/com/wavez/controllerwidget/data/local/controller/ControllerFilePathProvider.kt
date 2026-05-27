package com.wavez.controllerwidget.data.local.controller

import android.content.Context
import com.wavez.controllerwidget.data.local.ThemeStorageConfig
import com.wavez.controllerwidget.data.local.sanitize
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
