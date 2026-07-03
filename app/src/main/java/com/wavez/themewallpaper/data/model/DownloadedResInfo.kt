package com.wavez.themewallpaper.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wavez.themewallpaper.data.model.theme.ThemeModel
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel

@JsonClass(generateAdapter = true)
data class DownloadedResInfo(
    @param:Json(name = "type")
    val type: String,
    @param:Json(name = "id")
    val id: String
) {

    companion object {
        private const val TYPE_WALLPAPER = "wallpaper"
        private const val TYPE_CONTROLLER = "controller"
        private const val TYPE_THEME = "theme"


        fun fromResource(res: ThemeRemoteItem): DownloadedResInfo? {
            val resType = when (res) {
                is WallpaperModel -> TYPE_WALLPAPER
                is ControllerModel -> TYPE_CONTROLLER
                else -> return null
            }
            return DownloadedResInfo(resType, res.id)
        }

        fun fromTheme(theme: ThemeModel): DownloadedResInfo =
            DownloadedResInfo(TYPE_THEME, theme.id)
    }

}
