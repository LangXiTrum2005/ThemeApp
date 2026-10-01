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

typealias ControllerListResponse = ApiResponse<List<ControllerModel>>

interface Controller : ThemeRemoteItem {
    val like: Int
    val view: Int
}

@JsonClass(generateAdapter = true)
data class ControllerModel(
    @param:Json(name = "id")
    override val id: String = "",
    @param:Json(name = "name")
     val name: String,
    @param:Json(name = "thumb")
    override val thumb: String,
    @param:Json(name = "is_vip")
    override val premium: Int,
    @param:Json(name = "url")
    override val url: String,
    @param:Json(name = "category_id")
    override val categoryId: String,
) : ThemeRemoteItem
