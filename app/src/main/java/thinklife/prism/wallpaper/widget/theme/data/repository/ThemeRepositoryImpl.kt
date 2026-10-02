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

package thinklife.prism.wallpaper.widget.theme.data.repository

import android.util.Log
import thinklife.prism.wallpaper.widget.theme.di.IoDispatcher
import thinklife.prism.wallpaper.widget.theme.data.local.dao.DownloadedThemeDao
import thinklife.prism.wallpaper.widget.theme.data.local.entity.DownloadedThemeEntity
import thinklife.prism.wallpaper.widget.theme.data.model.DownloadedResInfo
import thinklife.prism.wallpaper.widget.theme.data.model.PopularCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeModel
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeWallpaperModel
import thinklife.prism.wallpaper.widget.theme.data.network.api.ApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val downloadedThemeDao: DownloadedThemeDao,
    private val sharedPref: SharedPref,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ThemeRepository {

    private var cachedCategories: List<ThemeCategoryModel>? = null
    private var cachedThemes: List<ThemeModel>? = null
    private var cachedMostPopularThemes: List<PopularCategoryModel>? = null

    override suspend fun getThemeCategories(
        forceRefresh: Boolean,
    ): Result<List<ThemeCategoryModel>> {
        if (!forceRefresh) {
            cachedCategories?.let { return Result.success(it) }
        }
        return repositoryRequest { apiService.getThemeCategories() }
            .onSuccess { cachedCategories = it }
    }

    override suspend fun getThemes(forceRefresh: Boolean): Result<List<ThemeModel>> {
        if (!forceRefresh) {
            cachedThemes?.let { return Result.success(it) }
        }
        return repositoryRequest { apiService.getThemes() }
            .onSuccess { cachedThemes = it }
    }

    override suspend fun getMostPopularThemes(forceRefresh: Boolean): Result<List<PopularCategoryModel>> {
        if (!forceRefresh) {
            cachedMostPopularThemes?.let { return Result.success(it) }
        }
        return repositoryRequest { apiService.getMostPopularThemes() }
            .onSuccess { cachedMostPopularThemes = it }
    }

    override suspend fun getThemeById(
        themeId: String,
        forceRefresh: Boolean,
    ): Result<ThemeModel> {
        return getThemes(forceRefresh).mapCatching { themes ->
            themes.firstOrNull { it.id == themeId }
                ?: error("Theme not found")
        }
    }

    override suspend fun getThemesByCategory(
        categoryId: String,
        forceRefresh: Boolean,
    ): Result<List<ThemeModel>> {
        return getThemes(forceRefresh).map { themes ->
            themes.filter { it.categoryId == categoryId }
        }
    }

    override suspend fun saveTheme(theme: ThemeModel): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            downloadedThemeDao.upsert(theme.toDownloadedEntity())
            runCatching {
                apiService.updateWallpaperDownloadAmount(DownloadedResInfo.fromTheme(theme))
            }.onFailure {
                Log.e("WallpaperRepository", "updateWallpaperDownloadAmount failed", it)
            }
            Unit
        }
    }

    override suspend fun checkThemeSaved(themeId: String): Boolean = withContext(ioDispatcher) {
        downloadedThemeDao.getByThemeId(themeId) != null
    }

    override fun observeDownloadedThemes(): Flow<List<ThemeModel>> =
        downloadedThemeDao.getThemeAll()
            .map { entities -> entities.map { it.toThemeModel() } }
            .flowOn(ioDispatcher)

    private fun ThemeModel.toDownloadedEntity(): DownloadedThemeEntity {
        return DownloadedThemeEntity(
            themeId = id,
            name = name,
            thumb = thumb,
            wallpaperThumb = wallpaper.thumb,
            wallpaperUrl = wallpaper.url,
            downloadedAt = System.currentTimeMillis(),
        )
    }

    private fun DownloadedThemeEntity.toThemeModel(): ThemeModel {
        return ThemeModel(
            id = themeId,
            name = name,
            thumb = thumb,
            premium = 0,
            categoryId = "",
            wallpaper = ThemeWallpaperModel(
                id = themeId,
                thumb = wallpaperThumb,
                url = wallpaperUrl,
            ),
            icons = emptyList(),
        )
    }
}
