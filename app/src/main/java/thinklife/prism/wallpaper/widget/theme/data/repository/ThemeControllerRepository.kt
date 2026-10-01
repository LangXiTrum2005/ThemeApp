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

import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerConfigModel
import thinklife.prism.wallpaper.widget.theme.core.download.DownloadState
import thinklife.prism.wallpaper.widget.theme.data.model.ControllerCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.model.ControllerModel
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
