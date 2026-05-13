package com.wavez.controllerwidget.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.controllerwidget.data.network.model.ApiResponse

typealias WallpaperListResponse = ApiResponse<List<WallpaperModel>>

@JsonClass(generateAdapter = true)
data class WallpaperModel(
    @param:Json(name = "id")
    override val id: String = "",
    @param:Json(name = "name")
    override val name: String,
    @param:Json(name = "thumb")
    override val thumb: String,
    @param:Json(name = "premium")
    override val premium: Int,
    @param:Json(name = "url")
    override val url: String,
    @param:Json(name = "category_id")
    override val categoryId: String,
) : ThemeRemoteItem
