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
import thinklife.prism.wallpaper.widget.theme.data.network.model.ApiResponse

typealias PopularCategoryListResponse = ApiResponse<List<PopularCategoryModel>>

@JsonClass(generateAdapter = true)
data class PopularCategoryModel(
    @param:Json(name = "category")
    val category: PopularCategory,
    @param:Json(name = "total_downloads")
    val totalDownloads: Long,
)

@JsonClass(generateAdapter = true)
data class PopularCategory(
    @param:Json(name = "id")
    val id: String = "",
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "icon")
    val icon: String = "",
)
