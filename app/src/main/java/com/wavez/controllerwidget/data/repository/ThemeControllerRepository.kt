package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.data.model.ControllerCategoryModel
import com.wavez.controllerwidget.data.model.ControllerModel

interface ThemeControllerRepository {

    suspend fun getCategoriesController(
        forceRefresh: Boolean = false
    ): Result<List<ControllerCategoryModel>>

    suspend fun getAllControllerTheme(
        forceRefresh: Boolean = false
    ): Result<List<ControllerModel>>
}
