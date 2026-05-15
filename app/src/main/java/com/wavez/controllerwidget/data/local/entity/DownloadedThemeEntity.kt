package com.wavez.controllerwidget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wavez.controllerwidget.data.local.ThemeStorageConfig

@Entity(tableName = ThemeStorageConfig.DOWNLOADED_THEME_TABLE)
data class DownloadedThemeEntity(
    @PrimaryKey
    val themeId: String,
    val filePath: String,
    val downloadedAt: Long,
    val fileSize: Long,
)
