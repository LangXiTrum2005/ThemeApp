package com.wavez.controllerwidget.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.controllerwidget.data.network.model.ApiResponse

typealias WallpaperCategoryListResponse = ApiResponse<List<WallpaperCategoryModel>>
typealias ControllerCategoryListResponse = ApiResponse<List<ControllerCategoryModel>>

@JsonClass(generateAdapter = true)
data class WallpaperCategoryModel(
    @param:Json(name = "id")
    override val id: String = "",
    @param:Json(name = "name")
    override val name: String,
    @param:Json(name = "icon")
    override val icon: String,
) : ThemeCategory

@JsonClass(generateAdapter = true)
data class ControllerCategoryModel(
    @param:Json(name = "id")
    override val id: String = "",
    @param:Json(name = "name")
    override val name: String,
    @param:Json(name = "icon")
    override val icon: String,
) : ThemeCategory
