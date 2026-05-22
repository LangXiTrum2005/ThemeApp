package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.data.local.dao.DownloadedThemeDao
import com.wavez.controllerwidget.data.local.entity.DownloadedThemeEntity
import com.wavez.controllerwidget.data.model.theme.ThemeCategoryModel
import com.wavez.controllerwidget.data.model.theme.ThemeModel
import com.wavez.controllerwidget.data.model.theme.ThemeWallpaperModel
import com.wavez.controllerwidget.data.network.api.ControllerWidgetApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val apiService: ControllerWidgetApiService,
    private val downloadedThemeDao: DownloadedThemeDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ThemeRepository {

    private var cachedCategories: List<ThemeCategoryModel>? = null
    private var cachedThemes: List<ThemeModel>? = null

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
