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

package thinklife.prism.wallpaper.widget.theme.data.model.theme

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconItemModel
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetsModel
import thinklife.prism.wallpaper.widget.theme.data.network.model.ApiResponse
import kotlinx.parcelize.Parcelize

typealias ThemeListResponse = ApiResponse<List<ThemeModel>>

@JsonClass(generateAdapter = true)
@Parcelize
data class ThemeModel(
    @param:Json(name = "id")
    val id: String = "",
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "thumbnail_url")
    val thumb: String,
    @param:Json(name = "is_premium")
    val premium: Int,
    @param:Json(name = "category_id")
    val categoryId: String,
    @param:Json(name = "wallpaper")
    val wallpaper: ThemeWallpaperModel,
    @param:Json(name = "icons")
    val icons: List<IconItemModel>,
    @param:Json(name = "tags")
    val tags: List<String> = emptyList(),
    @param:Json(name = "widget")
    val widget: ThemeWidgetModel? = null,
) : Parcelable {
    /** Builds a [WidgetsModel] from this theme's widget, or null when the theme has none. */
    fun toWidgetsModel(): WidgetsModel? {
        val widget = widget ?: return null
        return WidgetsModel(
            id = id,
            name = name,
            thumb = widget.contentM,
            contentS = widget.contentS,
            contentM = widget.contentM,
            contentL = widget.contentL,
            premium = premium,
            categoryId = categoryId,
        )
    }
}


@JsonClass(generateAdapter = true)
@Parcelize
data class ThemeWallpaperModel(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "thumb") val thumb: String,
    @param:Json(name = "url") val url: String,
) : Parcelable


@JsonClass(generateAdapter = true)
@Parcelize
data class ThemeWidgetModel(
    @param:Json(name = "content_s") val contentS: String,
    @param:Json(name = "content_m") val contentM: String,
    @param:Json(name = "content_l") val contentL: String,
) : Parcelable

