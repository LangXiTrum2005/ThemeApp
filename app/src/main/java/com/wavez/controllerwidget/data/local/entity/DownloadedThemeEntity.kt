package com.wavez.controllerwidget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wavez.controllerwidget.data.local.ThemeStorageConfig

@Entity(tableName = ThemeStorageConfig.DOWNLOADED_P67_THEME_TABLE)
data class DownloadedThemeEntity(
    @PrimaryKey
    val themeId: String,
    val name: String,
    val thumb: String,
    val wallpaperThumb: String,
    val wallpaperUrl: String,
    val downloadedAt : Long,
)
