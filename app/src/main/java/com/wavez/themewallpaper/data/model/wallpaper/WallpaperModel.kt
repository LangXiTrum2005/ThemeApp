package com.wavez.themewallpaper.data.model.wallpaper

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.themewallpaper.data.model.ThemeRemoteItem
import com.wavez.themewallpaper.data.network.model.ApiResponse
import kotlinx.parcelize.Parcelize

typealias WallpaperListResponse = ApiResponse<List<WallpaperModel>>

@JsonClass(generateAdapter = true)
@Parcelize
data class WallpaperModel(
    @param:Json(name = "id")
    override val id: String = "",
    @param:Json(name = "thumb_small")
    override val thumb: String,
    @param:Json(name = "is_vip")
    override val premium: Int,
    @param:Json(name = "url")
    override val url: String,
    @param:Json(name = "category_id")
    override val categoryId: String,
    @param:Json(name = "tags")
    val tags: List<String> = emptyList(),
) : ThemeRemoteItem, Parcelable {
        fun isVip(): Boolean {
            return premium == 1
        }
}
