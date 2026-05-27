package com.wavez.controllerwidget.data.model.icon

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.controllerwidget.data.network.model.ApiResponse
import kotlinx.parcelize.Parcelize

typealias IconPackListResponse = ApiResponse<List<IconPackModel>>

@JsonClass(generateAdapter = true)
data class IconPackModel(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "thumbnail_url") val thumb: String,
    @param:Json(name = "is_premium") val premium: Int,
    @param:Json(name = "icons") val icons: List<IconItemModel>? = null,
) {
    fun isVip(): Boolean {
        return premium == 1
    }
}

@JsonClass(generateAdapter = true)
@Parcelize
data class IconItemModel(
    @param:Json(name = "package_name") val packageName: String,
    @param:Json(name = "app_name") val appName: String,
    @param:Json(name = "icon_url") val iconUrl: String,
) : Parcelable
