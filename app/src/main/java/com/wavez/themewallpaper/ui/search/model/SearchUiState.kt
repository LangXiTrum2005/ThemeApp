package com.wavez.themewallpaper.ui.search.model

import com.wavez.themewallpaper.data.model.icon.IconPackModel
import com.wavez.themewallpaper.data.model.theme.ThemeModel
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel

data class SearchUiState(
    val isLoading: Boolean = false,
    val mode: SearchMode = SearchMode.THEMES,
    val query: String = "",
    val popularCategories: List<SearchCategoryUi> = emptyList(),
    val themes: List<ThemeModel> = emptyList(),
    val wallpapers: List<WallpaperModel> = emptyList(),
    val iconPacks: List<IconPackModel> = emptyList(),
    val message: String? = null,
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val hasResults: Boolean
        get() = when (mode) {
            SearchMode.THEMES -> themes.isNotEmpty()
            SearchMode.WALLPAPERS -> wallpapers.isNotEmpty()
            SearchMode.ICONS -> iconPacks.isNotEmpty()
        }
    val isEmpty: Boolean get() = !isLoading && message == null && isSearching && !hasResults
}

data class SearchCategoryUi(
    val id: String,
    val name: String,
)
