package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.data.model.WallpaperCategoryModel
import com.wavez.controllerwidget.data.model.WallpaperModel
import com.wavez.controllerwidget.data.network.api.ControllerWidgetApiService
import javax.inject.Inject

class WallpaperRepositoryImpl @Inject constructor(
    private val apiService: ControllerWidgetApiService,
) : WallpaperRepository {

    private var cachedCategories: List<WallpaperCategoryModel>? = null
    private var cachedWallpapers: List<WallpaperModel>? = null

    override suspend fun getCategoriesUnlimited(
        forceRefresh: Boolean
    ): Result<List<WallpaperCategoryModel>> {
        if (!forceRefresh) {
            cachedCategories?.let { return Result.success(it) }
        }

        return repositoryRequest { apiService.getCategoriesUnlimited() }
            .onSuccess { cachedCategories = it }
    }

    override suspend fun getAllWallpapersUnlimited(
        forceRefresh: Boolean
    ): Result<List<WallpaperModel>> {
        if (!forceRefresh) {
            cachedWallpapers?.let { return Result.success(it) }
        }

        return repositoryRequest { apiService.getAllWallpapersUnlimited() }
            .onSuccess { cachedWallpapers = it }
    }

    private fun clearSessionCache() {
        cachedCategories = null
        cachedWallpapers = null
    }

}
