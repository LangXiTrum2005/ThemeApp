package com.wavez.themewallpaper.ui.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.extensions.normalized
import com.wavez.themewallpaper.data.model.PopularCategoryModel
import com.wavez.themewallpaper.data.model.WallpaperCategoryModel
import com.wavez.themewallpaper.data.model.icon.IconPackModel
import com.wavez.themewallpaper.data.model.theme.ThemeCategoryModel
import com.wavez.themewallpaper.data.model.theme.ThemeModel
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel
import com.wavez.themewallpaper.data.model.widget.WidgetCategoryModel
import com.wavez.themewallpaper.data.model.widget.WidgetsModel
import com.wavez.themewallpaper.data.repository.IconPackRepository
import com.wavez.themewallpaper.data.repository.ThemeRepository
import com.wavez.themewallpaper.data.repository.WallpaperRepository
import com.wavez.themewallpaper.data.repository.WidgetRepository
import com.wavez.themewallpaper.ui.search.model.SearchCategoryUi
import com.wavez.themewallpaper.ui.search.model.SearchMode
import com.wavez.themewallpaper.ui.search.model.SearchUiState
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
    private val iconPackRepository: IconPackRepository,
    private val widgetRepository: WidgetRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var themeCategories: List<ThemeCategoryModel> = emptyList()
    private var themes: List<ThemeModel> = emptyList()
    private var mostPopularThemeCategories: List<PopularCategoryModel> = emptyList()
    private var wallpaperCategories: List<WallpaperCategoryModel> = emptyList()
    private var wallpapers: List<WallpaperModel> = emptyList()
    private var mostPopularWallpaperCategories: List<PopularCategoryModel> = emptyList()
    private var iconPacks: List<IconPackModel> = emptyList()
    private var mostPopularIconCategories: List<PopularCategoryModel> = emptyList()
    private var widgetCategories: List<WidgetCategoryModel> = emptyList()
    private var widgets: List<WidgetsModel> = emptyList()
    private var mode: SearchMode = SearchMode.THEMES

    fun load(mode: SearchMode = this.mode, forceRefresh: Boolean = false) {
        this.mode = mode
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(mode = mode, isLoading = true, message = null)
            when (mode) {
                SearchMode.THEMES -> loadThemes(forceRefresh)
                SearchMode.WALLPAPERS -> loadWallpapers(forceRefresh)
                SearchMode.ICONS -> loadIcons(forceRefresh)
                SearchMode.WIDGETS -> loadWidgets(forceRefresh)
            }
        }
    }

    fun search(query: String) {
        publish(query = query, isLoading = false)
    }

    private suspend fun loadThemes(forceRefresh: Boolean) {
        val categoriesResult = themeRepository.getThemeCategories(forceRefresh)
        val themesResult = themeRepository.getThemes(forceRefresh)
        val mostPopularResult = themeRepository.getMostPopularThemes(forceRefresh)

        categoriesResult.onSuccess { themeCategories = it }
        themesResult.onSuccess { themes = it }
        mostPopularResult.onSuccess { mostPopularThemeCategories = it }

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
        val mostPopularResult = wallpaperRepository.getMostPopularWallpapers(forceRefresh)

        categoriesResult.onSuccess { wallpaperCategories = it }
        wallpapersResult.onSuccess { wallpapers = it }
        mostPopularResult.onSuccess { mostPopularWallpaperCategories = it }

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

    private suspend fun loadIcons(forceRefresh: Boolean) {
        val iconsResult = iconPackRepository.getIconPacks(forceRefresh)
        val mostPopularResult = iconPackRepository.getMostPopularIcons(forceRefresh)

        iconsResult.onSuccess { iconPacks = it }
        mostPopularResult.onSuccess { mostPopularIconCategories = it }

        val failure = iconsResult.exceptionOrNull()
        if (failure != null && iconPacks.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = failure.message.orEmpty().ifBlank { "Could not load content" },
            )
            return
        }

        publish(query = _uiState.value.query, isLoading = false)
    }

    private suspend fun loadWidgets(forceRefresh: Boolean) {
        val categoriesResult = widgetRepository.getCategories(forceRefresh)
        val widgetsResult = widgetRepository.getAllWidgets(forceRefresh)

        categoriesResult.onSuccess { widgetCategories = it }
        widgetsResult.onSuccess { widgets = it }

        val failure = categoriesResult.exceptionOrNull() ?: widgetsResult.exceptionOrNull()
        if (failure != null && widgets.isEmpty()) {
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
            SearchMode.ICONS -> buildIconState(cleanQuery, normalizedQuery, isLoading)
            SearchMode.WIDGETS -> buildWidgetState(cleanQuery, normalizedQuery, isLoading)
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
            popularCategories = mostPopularThemeCategories.map {
                SearchCategoryUi(id = it.category.id, name = it.category.name)
            }.ifEmpty {
                themeCategories.map { SearchCategoryUi(id = it.id, name = it.name) }
            }.take(MAX_POPULAR_CATEGORY_COUNT),
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
            popularCategories = mostPopularWallpaperCategories.map {
                SearchCategoryUi(id = it.category.id, name = it.category.name)
            }.ifEmpty {
                wallpaperCategories.map { SearchCategoryUi(id = it.id, name = it.name) }
            }.take(MAX_POPULAR_CATEGORY_COUNT),
            wallpapers = wallpapers.filterWallpapersByQuery(normalizedQuery, categoryNamesById),
        )
    }

    private fun buildIconState(
        cleanQuery: String,
        normalizedQuery: String,
        isLoading: Boolean,
    ): SearchUiState {
        return SearchUiState(
            isLoading = isLoading,
            mode = SearchMode.ICONS,
            query = cleanQuery,
            popularCategories = mostPopularIconCategories.map {
                SearchCategoryUi(id = it.category.id, name = it.category.name)
            }.take(MAX_POPULAR_CATEGORY_COUNT),
            iconPacks = iconPacks.filterIconPacksByName(normalizedQuery),
        )
    }

    private fun buildWidgetState(
        cleanQuery: String,
        normalizedQuery: String,
        isLoading: Boolean,
    ): SearchUiState {
        val categoryNamesById = widgetCategories.associate { it.id to it.name }
        return SearchUiState(
            isLoading = isLoading,
            mode = SearchMode.WIDGETS,
            query = cleanQuery,
            popularCategories = widgetCategories
                .map { SearchCategoryUi(id = it.id, name = it.name) }
                .take(MAX_POPULAR_CATEGORY_COUNT),
            widgets = widgets.filterWidgetsByQuery(normalizedQuery, categoryNamesById),
        )
    }

    private fun List<ThemeModel>.filterThemesByQuery(
        normalizedQuery: String,
        categoryNamesById: Map<String, String>,
    ): List<ThemeModel> {
        if (normalizedQuery.isBlank()) return this
        return filter { item ->
            categoryNamesById[item.categoryId].orEmpty().normalized().contains(normalizedQuery) ||
                    item.tags.any { tag ->
                        tag.normalized().contains(normalizedQuery)
                    }
        }
    }

    private fun List<WallpaperModel>.filterWallpapersByQuery(
        normalizedQuery: String,
        categoryNamesById: Map<String, String>,
    ): List<WallpaperModel> {
        if (normalizedQuery.isBlank()) return this
        return filter { item ->
            categoryNamesById[item.categoryId].orEmpty().normalized().contains(normalizedQuery) ||
                    item.tags.any { tag ->
                        tag.normalized().contains(normalizedQuery)
                    }
        }
    }

    private fun List<IconPackModel>.filterIconPacksByName(normalizedQuery: String): List<IconPackModel> {
        if (normalizedQuery.isBlank()) return this
        return filter { item -> item.name.normalized().contains(normalizedQuery) }
    }

    private fun List<WidgetsModel>.filterWidgetsByQuery(
        normalizedQuery: String,
        categoryNamesById: Map<String, String>,
    ): List<WidgetsModel> {
        if (normalizedQuery.isBlank()) return this
        return filter { item ->
            item.name.normalized().contains(normalizedQuery) ||
                    categoryNamesById[item.categoryId].orEmpty().normalized()
                        .contains(normalizedQuery) ||
                    item.tags.any { tag -> tag.normalized().contains(normalizedQuery) }
        }
    }


    private companion object {
        const val MAX_POPULAR_CATEGORY_COUNT = 4
    }
}
