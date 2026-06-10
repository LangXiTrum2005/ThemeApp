package com.wavez.themewallpaper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wavez.themewallpaper.data.local.ThemeStorageConfig

@Entity(tableName = ThemeStorageConfig.DOWNLOADED_THEME_TABLE)
data class DownloadedControllerEntity(
    @PrimaryKey
    val themeId: String,
    val filePath: String,
    val downloadedAt: Long,
    val fileSize: Long,
)
