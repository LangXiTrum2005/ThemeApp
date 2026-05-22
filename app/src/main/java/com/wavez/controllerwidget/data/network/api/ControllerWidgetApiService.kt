package com.wavez.controllerwidget.data.network.api

import com.wavez.controllerwidget.data.model.ControllerCategoryListResponse
import com.wavez.controllerwidget.data.model.ControllerListResponse
import com.wavez.controllerwidget.data.model.DownloadedResInfo
import com.wavez.controllerwidget.data.model.WallpaperCategoryListResponse
import com.wavez.controllerwidget.data.model.icon.IconPackListResponse
import com.wavez.controllerwidget.data.model.theme.ThemeCategoryListResponse
import com.wavez.controllerwidget.data.model.theme.ThemeListResponse
import com.wavez.controllerwidget.data.model.wallpaper.WallpaperListResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ControllerWidgetApiService {

    //theme

    @GET("p67themecategory")
    suspend fun getThemeCategories(): ThemeCategoryListResponse

    @GET("p67themes")
    suspend fun getThemes(): ThemeListResponse

    //controller
    @GET("p67getcontrollercategory")
    suspend fun getCategoriesController(): ControllerCategoryListResponse

    @GET("p67getallcontrollerthemes")
    suspend fun getAllControllerTheme(): ControllerListResponse

    //wallpaper
    @GET("p67getwallpapercategory")
    suspend fun getCategoriesUnlimited(): WallpaperCategoryListResponse

    @GET("p67getallwallpapers")
    suspend fun getAllWallpapersUnlimited(): WallpaperListResponse

    @POST("api/updatedownloadp67")
    suspend fun updateWallpaperDownloadAmount(@Body info: DownloadedResInfo): Result<ResponseBody>

    //icon packs
    @GET("p67iconpacks")
    suspend fun getIconPacks(): IconPackListResponse

}
