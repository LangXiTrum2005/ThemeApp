package com.wavez.controllerwidget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wavez.controllerwidget.data.local.WallpaperStorageConfig

@Entity(tableName = WallpaperStorageConfig.DOWNLOADED_WALLPAPER_TABLE)
data class DownloadedWallpaperEntity(
    @PrimaryKey
    val wallpaperId: String,
    val filePath: String,
    val downloadedAt: Long,
    val fileSize: Long,
)
