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

package thinklife.prism.wallpaper.widget.theme.data.model.widget

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import thinklife.prism.wallpaper.widget.theme.data.network.model.ApiResponse
import kotlinx.parcelize.Parcelize

typealias WidgetsListResponse = ApiResponse<List<WidgetsModel>>

@JsonClass(generateAdapter = true)
@Parcelize
data class WidgetsModel(
    @param:Json(name = "id")
    val id: String = "",
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "thumbnail_url")
    val thumb: String,
    @param:Json(name = "content_s")
    val contentS: String,
    @param:Json(name = "content_m")
    val contentM: String,
    @param:Json(name = "content_l")
    val contentL: String,
    @param:Json(name = "is_premium")
    val premium: Int,
    @param:Json(name = "category_id")
    val categoryId: String,
    @param:Json(name = "tags")
    val tags: List<String> = emptyList(),
) : Parcelable

