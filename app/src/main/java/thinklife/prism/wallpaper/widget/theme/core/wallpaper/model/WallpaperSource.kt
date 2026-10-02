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

package thinklife.prism.wallpaper.widget.theme.core.wallpaper.model

import android.content.ComponentName
import android.net.Uri

sealed class WallpaperSource {
    data class StaticImage(val uri: Uri) : WallpaperSource()
    data class OnlineImage(val url: String) : WallpaperSource()
    data class Gif(val uri: Uri) : WallpaperSource()
    data class Video(val uri: Uri) : WallpaperSource()
    data class Live(val componentName: ComponentName) : WallpaperSource()
}
