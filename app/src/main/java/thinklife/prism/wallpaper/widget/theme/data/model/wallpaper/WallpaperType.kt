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

package thinklife.prism.wallpaper.widget.theme.data.model.wallpaper

enum class WallpaperType { IMAGE, GIF, VIDEO }

val WallpaperModel.wallpaperType: WallpaperType
    get() {
        // Strip query string and fragment before checking extension
        val path = url.substringBefore('?').substringBefore('#').lowercase()
        return when {
            path.endsWith(".mp4") || path.endsWith(".webm") ||
            path.endsWith(".mov") || path.endsWith(".avi") -> WallpaperType.VIDEO
            path.endsWith(".gif") -> WallpaperType.GIF
            else -> WallpaperType.IMAGE
        }
    }
