package com.wavez.themewallpaper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wavez.themewallpaper.data.local.WidgetStorageConfig

@Entity(tableName = WidgetStorageConfig.DOWNLOADED_WIDGET_TABLE)
data class DownloadedWidgetEntity(
    @PrimaryKey
    val widgetId: String,
    val name: String,
    val thumb: String,
    val contentS: String,
    val contentM: String,
    val contentL: String,
    val premium: Int,
    val categoryId: String,
    val addedAt: Long,
)
