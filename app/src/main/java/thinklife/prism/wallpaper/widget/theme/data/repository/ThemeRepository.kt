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

import thinklife.prism.wallpaper.widget.theme.data.model.PopularCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeModel
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {

    suspend fun getThemeCategories(forceRefresh: Boolean = false): Result<List<ThemeCategoryModel>>

    suspend fun getThemes(forceRefresh: Boolean = false): Result<List<ThemeModel>>

    suspend fun getMostPopularThemes(forceRefresh: Boolean = false): Result<List<PopularCategoryModel>>

    suspend fun getThemeById(themeId: String, forceRefresh: Boolean = false): Result<ThemeModel>

    suspend fun getThemesByCategory(
        categoryId: String,
        forceRefresh: Boolean = false,
    ): Result<List<ThemeModel>>

    suspend fun saveTheme(theme: ThemeModel): Result<Unit>

    suspend fun checkThemeSaved(themeId: String): Boolean

    fun observeDownloadedThemes(): Flow<List<ThemeModel>>

}
