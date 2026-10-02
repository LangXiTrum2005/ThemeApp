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

    @GET(ApiEndpoints.THEME_CATEGORIES)
    suspend fun getThemeCategories(): ThemeCategoryListResponse

    @GET(ApiEndpoints.THEMES)
    suspend fun getThemes(): ThemeListResponse

    @GET(ApiEndpoints.MOST_POPULAR_THEMES)
    suspend fun getMostPopularThemes(): PopularCategoryListResponse

    //controller
    @GET(ApiEndpoints.CONTROLLER_THEMES)
    suspend fun getAllControllerTheme(): ControllerListResponse

    //wallpaper
    @GET(ApiEndpoints.WALLPAPER_CATEGORIES)
    suspend fun getCategoriesUnlimited(): WallpaperCategoryListResponse

    @GET(ApiEndpoints.WALLPAPERS)
    suspend fun getAllWallpapersUnlimited(): WallpaperListResponse

    @GET(ApiEndpoints.MOST_POPULAR_WALLPAPERS)
    suspend fun getMostPopularWallpapers(): PopularCategoryListResponse

    @POST(ApiEndpoints.UPDATE_DOWNLOAD_AMOUNT)
    suspend fun updateWallpaperDownloadAmount(@Body info: DownloadedResInfo): Response<Unit>

    //icon packs
    @GET(ApiEndpoints.ICON_PACKS)
    suspend fun getIconPacks(): IconPackListResponse

    @GET(ApiEndpoints.MOST_POPULAR_ICONS)
    suspend fun getMostPopularIcons(): PopularCategoryListResponse

    //widget
    @GET(ApiEndpoints.WIDGET_CATEGORIES)
    suspend fun getWidgetCategories(): WidgetCategoryListResponse

    @GET(ApiEndpoints.WIDGETS)
    suspend fun getAllWidgets(): WidgetsListResponse

}
