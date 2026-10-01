package com.wavez.themewallpaper.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.themewallpaper.data.network.model.ApiResponse

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
