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

package thinklife.prism.wallpaper.widget.theme.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeModel
import thinklife.prism.wallpaper.widget.theme.data.model.wallpaper.WallpaperModel

@JsonClass(generateAdapter = true)
data class DownloadedResInfo(
    @param:Json(name = "type")
    val type: String,
    @param:Json(name = "id")
    val id: String
) {

    companion object {
        private const val TYPE_WALLPAPER = "wallpaper"
        private const val TYPE_CONTROLLER = "controller"
        private const val TYPE_THEME = "theme"


        fun fromResource(res: ThemeRemoteItem): DownloadedResInfo? {
            val resType = when (res) {
                is WallpaperModel -> TYPE_WALLPAPER
                is ControllerModel -> TYPE_CONTROLLER
                else -> return null
            }
            return DownloadedResInfo(resType, res.id)
        }

        fun fromTheme(theme: ThemeModel): DownloadedResInfo =
            DownloadedResInfo(TYPE_THEME, theme.id)
    }

}
