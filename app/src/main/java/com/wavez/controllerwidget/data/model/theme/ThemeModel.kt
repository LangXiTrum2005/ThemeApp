package com.wavez.controllerwidget.data.model.theme

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.controllerwidget.data.model.icon.IconItemModel
import com.wavez.controllerwidget.data.network.model.ApiResponse
import kotlinx.parcelize.Parcelize

typealias ThemeListResponse = ApiResponse<List<ThemeModel>>

@JsonClass(generateAdapter = true)
@Parcelize
data class ThemeModel(
    @param:Json(name = "id")
    val id: String = "",
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "thumbnail_url")
    val thumb: String,
    @param:Json(name = "is_premium")
    val premium: Int,
    @param:Json(name = "category_id")
    val categoryId: String,
    @param:Json(name = "wallpaper")
    val wallpaper: ThemeWallpaperModel,
    @param:Json(name = "icons")
    val icons: List<IconItemModel>,
    @param:Json(name = "tags")
    val tags: List<String> = emptyList(),
) : Parcelable {
    fun isVip(): Boolean {
        return premium == 1
    }
}


@JsonClass(generateAdapter = true)
@Parcelize
data class ThemeWallpaperModel(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "thumb") val thumb: String,
    @param:Json(name = "url") val url: String,
) : Parcelable

