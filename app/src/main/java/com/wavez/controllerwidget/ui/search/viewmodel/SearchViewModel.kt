package com.wavez.controllerwidget.ui.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.data.model.WallpaperCategoryModel
import com.wavez.controllerwidget.data.model.theme.ThemeCategoryModel
import com.wavez.controllerwidget.data.model.theme.ThemeModel
import com.wavez.controllerwidget.data.model.wallpaper.WallpaperModel
import com.wavez.controllerwidget.data.repository.ThemeRepository
import com.wavez.controllerwidget.data.repository.WallpaperRepository
import com.wavez.controllerwidget.ui.search.model.SearchCategoryUi
import com.wavez.controllerwidget.ui.search.model.SearchMode
import com.wavez.controllerwidget.ui.search.model.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val wallpaperRepository: WallpaperRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var themeCategories: List<ThemeCategoryModel> = emptyList()
    private var themes: List<ThemeModel> = emptyList()
    private var wallpaperCategories: List<WallpaperCategoryModel> = emptyList()
    private var wallpapers: List<WallpaperModel> = emptyList()
    private var mode: SearchMode = SearchMode.THEMES

    fun load(mode: SearchMode = this.mode, forceRefresh: Boolean = false) {
        this.mode = mode
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(mode = mode, isLoading = true, message = null)
            when (mode) {
                SearchMode.THEMES -> loadThemes(forceRefresh)
                SearchMode.WALLPAPERS -> loadWallpapers(forceRefresh)
            }
        }
    }

    fun search(query: String) {
        publish(query = query, isLoading = false)
    }

    private suspend fun loadThemes(forceRefresh: Boolean) {
        val categoriesResult = themeRepository.getThemeCategories(forceRefresh)
        val themesResult = themeRepository.getThemes(forceRefresh)

        categoriesResult.onSuccess { themeCategories = it }
        themesResult.onSuccess { themes = it }

        val failure = categoriesResult.exceptionOrNull() ?: themesResult.exceptionOrNull()
        if (failure != null && themes.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = failure.message.orEmpty().ifBlank { "Could not load content" },
            )
            return
        }

        publish(query = _uiState.value.query, isLoading = false)
    }

    private suspend fun loadWallpapers(forceRefresh: Boolean) {
        val categoriesResult = wallpaperRepository.getCategoriesUnlimited(forceRefresh)
        val wallpapersResult = wallpaperRepository.getAllWallpapersUnlimited(forceRefresh)

        categoriesResult.onSuccess { wallpaperCategories = it }
        wallpapersResult.onSuccess { wallpapers = it }

        val failure = categoriesResult.exceptionOrNull() ?: wallpapersResult.exceptionOrNull()
        if (failure != null && wallpapers.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = failure.message.orEmpty().ifBlank { "Could not load content" },
            )
            return
        }

        publish(query = _uiState.value.query, isLoading = false)
    }

    private fun publish(query: String, isLoading: Boolean) {
        val cleanQuery = query.trim()
        val normalizedQuery = cleanQuery.normalized()
        val state = when (mode) {
            SearchMode.THEMES -> buildThemeState(cleanQuery, normalizedQuery, isLoading)
            SearchMode.WALLPAPERS -> buildWallpaperState(cleanQuery, normalizedQuery, isLoading)
        }
        _uiState.value = state
    }

    private fun buildThemeState(
        cleanQuery: String,
        normalizedQuery: String,
        isLoading: Boolean,
    ): SearchUiState {
        val categoryNamesById = themeCategories.associate { it.id to it.name }
        return SearchUiState(
            isLoading = isLoading,
            mode = SearchMode.THEMES,
            query = cleanQuery,
            popularCategories = themeCategories.take(MAX_POPULAR_CATEGORY_COUNT).map {
                SearchCategoryUi(id = it.id, name = it.name)
            },
            themes = themes.filterThemesByQuery(normalizedQuery, categoryNamesById),
        )
    }

    private fun buildWallpaperState(
        cleanQuery: String,
        normalizedQuery: String,
        isLoading: Boolean,
    ): SearchUiState {
        val categoryNamesById = wallpaperCategories.associate { it.id to it.name }
        return SearchUiState(
            isLoading = isLoading,
            mode = SearchMode.WALLPAPERS,
            query = cleanQuery,
            popularCategories = wallpaperCategories.take(MAX_POPULAR_CATEGORY_COUNT).map {
                SearchCategoryUi(id = it.id, name = it.name)
            },
            wallpapers = wallpapers.filterWallpapersByQuery(normalizedQuery, categoryNamesById),
        )
    }

    private fun List<ThemeModel>.filterThemesByQuery(
        normalizedQuery: String,
        categoryNamesById: Map<String, String>,
    ): List<ThemeModel> {
        if (normalizedQuery.isBlank()) return this
        return filter { item ->
            item.name.normalized().contains(normalizedQuery) ||
                categoryNamesById[item.categoryId].orEmpty().normalized().contains(normalizedQuery)
        }
    }

    private fun List<WallpaperModel>.filterWallpapersByQuery(
        normalizedQuery: String,
        categoryNamesById: Map<String, String>,
    ): List<WallpaperModel> {
        if (normalizedQuery.isBlank()) return this
        return filter { item ->
            item.name.normalized().contains(normalizedQuery) ||
                categoryNamesById[item.categoryId].orEmpty().normalized().contains(normalizedQuery)
        }
    }

    private fun String.normalized(): String = lowercase().filter { it.isLetterOrDigit() }

    private companion object {
        const val MAX_POPULAR_CATEGORY_COUNT = 8
    }
}
