package com.wavez.themewallpaper.data.model.widget

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.themewallpaper.data.model.ThemeCategory
import com.wavez.themewallpaper.data.network.model.ApiResponse
import com.wavez.themewallpaper.ui.home.dialog.FilterCategoryItem


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
