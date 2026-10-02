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
import thinklife.prism.wallpaper.widget.theme.data.local.ThemeStorageConfig

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
