package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.core.controllercenter.model.ControllerConfigModel
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.data.model.ControllerCategoryModel
import com.wavez.controllerwidget.data.model.ControllerModel
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ThemeControllerRepository {

    suspend fun getCategoriesController(
        forceRefresh: Boolean = false
    ): Result<List<ControllerCategoryModel>>

    suspend fun getAllControllerTheme(
        forceRefresh: Boolean = false
    ): Result<List<ControllerModel>>

    suspend fun checkDownloaded(themeId: String): Boolean

    suspend fun getLocalThemeFile(themeId: String): File?

    suspend fun applyTheme(theme: ControllerModel): Result<ControllerConfigModel>

    fun downloadControllerTheme(theme: ControllerModel): Flow<DownloadState<File>>
}
