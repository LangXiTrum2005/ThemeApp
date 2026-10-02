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

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import thinklife.prism.wallpaper.widget.theme.data.model.ThemeRemoteItem
import thinklife.prism.wallpaper.widget.theme.data.network.model.ApiResponse
import kotlinx.parcelize.Parcelize

typealias WallpaperListResponse = ApiResponse<List<WallpaperModel>>

@JsonClass(generateAdapter = true)
@Parcelize
data class WallpaperModel(
    @param:Json(name = "id")
    override val id: String = "",
    @param:Json(name = "thumb_small")
    override val thumb: String,
    @param:Json(name = "is_vip")
    override val premium: Int,
    @param:Json(name = "url")
    override val url: String,
    @param:Json(name = "category_id")
    override val categoryId: String,
    @param:Json(name = "tags")
    val tags: List<String> = emptyList(),
) : ThemeRemoteItem, Parcelable
