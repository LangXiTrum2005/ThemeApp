package com.wavez.themewallpaper.data.repository

import com.wavez.themewallpaper.core.controllercenter.model.ControllerConfigModel
import com.wavez.themewallpaper.core.download.DownloadState
import com.wavez.themewallpaper.data.model.ControllerCategoryModel
import com.wavez.themewallpaper.data.model.ControllerModel
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

    fun observeDownloadedThemes(): Flow<List<ControllerModel>>

    suspend fun applyTheme(theme: ControllerModel): Result<ControllerConfigModel>

    fun downloadControllerTheme(theme: ControllerModel): Flow<DownloadState<File>>
}
