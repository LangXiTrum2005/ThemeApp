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

package thinklife.prism.wallpaper.widget.theme.presentation.search.model

import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconPackModel
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeModel
import thinklife.prism.wallpaper.widget.theme.data.model.wallpaper.WallpaperModel
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetsModel

data class SearchUiState(
    val isLoading: Boolean = false,
    val mode: SearchMode = SearchMode.THEMES,
    val query: String = "",
    val popularCategories: List<SearchCategoryUi> = emptyList(),
    val themes: List<ThemeModel> = emptyList(),
    val wallpapers: List<WallpaperModel> = emptyList(),
    val iconPacks: List<IconPackModel> = emptyList(),
    val widgets: List<WidgetsModel> = emptyList(),
    val message: String? = null,
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val hasResults: Boolean
        get() = when (mode) {
            SearchMode.THEMES -> themes.isNotEmpty()
            SearchMode.WALLPAPERS -> wallpapers.isNotEmpty()
            SearchMode.ICONS -> iconPacks.isNotEmpty()
            SearchMode.WIDGETS -> widgets.isNotEmpty()
        }
    val isEmpty: Boolean get() = !isLoading && message == null && isSearching && !hasResults
}

data class SearchCategoryUi(
    val id: String,
    val name: String,
)
