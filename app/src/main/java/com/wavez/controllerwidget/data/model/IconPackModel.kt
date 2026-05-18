package com.wavez.controllerwidget.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.controllerwidget.data.network.model.ApiResponse

typealias IconPackListResponse = ApiResponse<List<IconPackModel>>
typealias IconPackDetailResponse = ApiResponse<IconPackModel>

@JsonClass(generateAdapter = true)
data class IconPackModel(
    @param:Json(name = "id") override val id: String,
    @param:Json(name = "name") override val name: String,
    @param:Json(name = "categoryId") override val categoryId: String,
    @param:Json(name = "thumbnail") override val thumb: String,
    @param:Json(name = "previewImages") val previewImages: List<String>,
    @param:Json(name = "iconCount") val iconCount: Int,
    @param:Json(name = "isPremium") override val premium: Int,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "url") override val url: String,
) : ThemeRemoteItem
