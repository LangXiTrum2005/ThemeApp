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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import thinklife.prism.wallpaper.widget.theme.data.model.ThemeCategory
import thinklife.prism.wallpaper.widget.theme.data.network.model.ApiResponse
import thinklife.prism.wallpaper.widget.theme.presentation.main.dialog.FilterCategoryItem


typealias WidgetCategoryListResponse = ApiResponse<List<WidgetCategoryModel>>

@JsonClass(generateAdapter = true)
data class WidgetCategoryModel(
    @param:Json(name = "id") override val id: String = "",
    @param:Json(name = "name") override val name: String,
    @param:Json(name = "icon") override val icon: String,
) : ThemeCategory {
    fun toFilterCategoryItem(): FilterCategoryItem {
        return FilterCategoryItem(
            id = id,
            title = name,
            iconUrl = icon,
        )
    }
}
