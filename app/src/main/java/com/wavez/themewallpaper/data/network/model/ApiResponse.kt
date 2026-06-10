package com.wavez.themewallpaper.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @param:Json(name = "status")
    val status: Int,
    @param:Json(name = "message")
    val message: String,
    @param:Json(name = "data")
    val data: T?,
) {
    val isSuccess: Boolean
        get() = status in 200..299
}
