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

package thinklife.prism.wallpaper.widget.theme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import thinklife.prism.wallpaper.widget.theme.data.local.WidgetStorageConfig

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
