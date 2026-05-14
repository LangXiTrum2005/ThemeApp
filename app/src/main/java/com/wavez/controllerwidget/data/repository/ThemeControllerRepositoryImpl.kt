package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.data.model.ControllerCategoryModel
import com.wavez.controllerwidget.data.model.ControllerModel
import com.wavez.controllerwidget.data.network.api.ControllerWidgetApiService
import javax.inject.Inject

class ThemeControllerRepositoryImpl @Inject constructor(
    private val apiService: ControllerWidgetApiService,
) : ThemeControllerRepository {

    private var cachedCategories: List<ControllerCategoryModel>? = null
    private var cachedControllerThemes: List<ControllerModel>? = null

    override suspend fun getCategoriesController(
        forceRefresh: Boolean
    ): Result<List<ControllerCategoryModel>> {
        if (!forceRefresh) {
            cachedCategories?.let { return Result.success(it) }
        }

        return repositoryRequest { apiService.getCategoriesController() }
            .onSuccess { cachedCategories = it }
    }

    override suspend fun getAllControllerTheme(
        forceRefresh: Boolean
    ): Result<List<ControllerModel>> {
        if (!forceRefresh) {
            cachedControllerThemes?.let { return Result.success(it) }
        }

        return repositoryRequest { apiService.getAllControllerTheme() }
            .onSuccess { cachedControllerThemes = it }
    }

}
