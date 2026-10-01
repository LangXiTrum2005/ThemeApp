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

package thinklife.prism.wallpaper.widget.theme.data.model.icon

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import thinklife.prism.wallpaper.widget.theme.data.network.model.ApiResponse
import kotlinx.parcelize.Parcelize

typealias IconPackListResponse = ApiResponse<List<IconPackModel>>

@JsonClass(generateAdapter = true)
data class IconPackModel(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "thumbnail_url") val thumb: String,
    @param:Json(name = "is_premium") val premium: Int,
    @param:Json(name = "icons") val icons: List<IconItemModel>? = null,
)

@JsonClass(generateAdapter = true)
@Parcelize
data class IconItemModel(
    @param:Json(name = "package_name") val packageName: String,
    @param:Json(name = "app_name") val appName: String,
    @param:Json(name = "icon_url") val iconUrl: String,
) : Parcelable
