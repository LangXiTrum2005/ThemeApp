package com.wavez.controllerwidget.ui.search.model

import com.wavez.controllerwidget.data.model.theme.ThemeModel
import com.wavez.controllerwidget.data.model.wallpaper.WallpaperModel

data class SearchUiState(
    val isLoading: Boolean = false,
    val mode: SearchMode = SearchMode.THEMES,
    val query: String = "",
    val popularCategories: List<SearchCategoryUi> = emptyList(),
    val themes: List<ThemeModel> = emptyList(),
    val wallpapers: List<WallpaperModel> = emptyList(),
    val message: String? = null,
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val hasResults: Boolean
        get() = when (mode) {
            SearchMode.THEMES -> themes.isNotEmpty()
            SearchMode.WALLPAPERS -> wallpapers.isNotEmpty()
        }
    val isEmpty: Boolean get() = !isLoading && message == null && isSearching && !hasResults
}

data class SearchCategoryUi(
    val id: String,
    val name: String,
)
