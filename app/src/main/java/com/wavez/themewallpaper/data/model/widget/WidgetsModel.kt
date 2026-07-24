package com.wavez.themewallpaper.data.model.widget

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.themewallpaper.data.network.model.ApiResponse
import kotlinx.parcelize.Parcelize

typealias WidgetsListResponse = ApiResponse<List<WidgetsModel>>

@JsonClass(generateAdapter = true)
@Parcelize
data class WidgetsModel(
    @param:Json(name = "id")
    val id: String = "",
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "thumbnail_url")
    val thumb: String,
    @param:Json(name = "content_s")
    val contentS: String,
    @param:Json(name = "content_m")
    val contentM: String,
    @param:Json(name = "content_l")
    val contentL: String,
    @param:Json(name = "is_premium")
    val premium: Int,
    @param:Json(name = "category_id")
    val categoryId: String,
    @param:Json(name = "tags")
    val tags: List<String> = emptyList(),
) : Parcelable {
    fun isVip(): Boolean {
        return premium == 1
    }
}

