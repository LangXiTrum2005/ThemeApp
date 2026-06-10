package com.wavez.themewallpaper.data.model.theme

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.themewallpaper.data.model.ThemeCategory
import com.wavez.themewallpaper.data.network.model.ApiResponse
import com.wavez.themewallpaper.ui.home.dialog.FilterCategoryItem


typealias ThemeCategoryListResponse = ApiResponse<List<ThemeCategoryModel>>

@JsonClass(generateAdapter = true)
data class ThemeCategoryModel(
    @param:Json(name = "id") override val id: String = "",
    @param:Json(name = "name") override val name: String,
    @param:Json(name = "icon") override val icon: String,
    @param:Json(name = "banner") val banner: String? = null,
) : ThemeCategory {
    fun toFilterCategoryItem(): FilterCategoryItem {
        return FilterCategoryItem(
            id = id,
            title = name,
            iconUrl = icon,
        )
    }
}
