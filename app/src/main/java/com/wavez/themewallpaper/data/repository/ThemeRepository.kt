package com.wavez.themewallpaper.data.repository

import com.wavez.themewallpaper.data.model.PopularCategoryModel
import com.wavez.themewallpaper.data.model.theme.ThemeCategoryModel
import com.wavez.themewallpaper.data.model.theme.ThemeModel
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

    fun checkUnlockWallpaper(theme: ThemeModel): Boolean

    fun unlockWallpaper(theme: ThemeModel)
}
