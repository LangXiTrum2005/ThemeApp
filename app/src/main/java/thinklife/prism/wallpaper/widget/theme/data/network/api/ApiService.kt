/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.data.network.api

import thinklife.prism.wallpaper.widget.theme.data.model.ControllerCategoryListResponse
import thinklife.prism.wallpaper.widget.theme.data.model.ControllerListResponse
import thinklife.prism.wallpaper.widget.theme.data.model.DownloadedResInfo
import thinklife.prism.wallpaper.widget.theme.data.model.PopularCategoryListResponse
import thinklife.prism.wallpaper.widget.theme.data.model.WallpaperCategoryListResponse
import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconPackListResponse
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeCategoryListResponse
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeListResponse
import thinklife.prism.wallpaper.widget.theme.data.model.wallpaper.WallpaperListResponse
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetCategoryListResponse
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetsListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

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

    //widget
    @GET("p67widgetcategory")
    suspend fun getWidgetCategories(): WidgetCategoryListResponse

    @GET("p67getallwidgets")
    suspend fun getAllWidgets(): WidgetsListResponse

}
