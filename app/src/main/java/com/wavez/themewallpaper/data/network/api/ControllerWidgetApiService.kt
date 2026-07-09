package com.wavez.themewallpaper.data.network.api

import com.wavez.themewallpaper.data.model.ControllerCategoryListResponse
import com.wavez.themewallpaper.data.model.ControllerListResponse
import com.wavez.themewallpaper.data.model.DownloadedResInfo
import com.wavez.themewallpaper.data.model.PopularCategoryListResponse
import com.wavez.themewallpaper.data.model.WallpaperCategoryListResponse
import com.wavez.themewallpaper.data.model.icon.IconPackListResponse
import com.wavez.themewallpaper.data.model.theme.ThemeCategoryListResponse
import com.wavez.themewallpaper.data.model.theme.ThemeListResponse
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperListResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ControllerWidgetApiService {

    //theme

    @GET("p67themecategory")
    suspend fun getThemeCategories(): ThemeCategoryListResponse

    @GET("p67themes")
    suspend fun getThemes(): ThemeListResponse

    @GET("p67mostpopularthemes")
    suspend fun getMostPopularThemes(): PopularCategoryListResponse

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

    @GET("p67mostpopularwallpapers")
    suspend fun getMostPopularWallpapers(): PopularCategoryListResponse

    @POST("api/updatedownloadp67")
    suspend fun updateWallpaperDownloadAmount(@Body info: DownloadedResInfo): Response<Unit>

    //icon packs
    @GET("p67iconpacks")
    suspend fun getIconPacks(): IconPackListResponse

    @GET("p67mostpopularicons")
    suspend fun getMostPopularIcons(): PopularCategoryListResponse

}
