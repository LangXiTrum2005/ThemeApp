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

package thinklife.prism.wallpaper.widget.theme.presentation.mygallery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeModel
import thinklife.prism.wallpaper.widget.theme.data.model.wallpaper.WallpaperModel
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetsModel
import thinklife.prism.wallpaper.widget.theme.data.repository.ThemeRepository
import thinklife.prism.wallpaper.widget.theme.data.repository.WallpaperRepository
import thinklife.prism.wallpaper.widget.theme.data.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyGalleryViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val widgetRepository: WidgetRepository,
) : ViewModel() {

    private val _themesState = MutableStateFlow<UiState<List<ThemeModel>>>(UiState.Idle)
    val themesState: StateFlow<UiState<List<ThemeModel>>> = _themesState.asStateFlow()

    private val _wallpapersState = MutableStateFlow<UiState<List<WallpaperModel>>>(UiState.Idle)
    val wallpapersState: StateFlow<UiState<List<WallpaperModel>>> = _wallpapersState.asStateFlow()

    private val _widgetsState = MutableStateFlow<UiState<List<WidgetsModel>>>(UiState.Idle)
    val widgetsState: StateFlow<UiState<List<WidgetsModel>>> = _widgetsState.asStateFlow()

    init {
        observeDownloadedThemes()
        observeDownloadedWallpapers()
        observeAddedWidgets()
    }

    private fun observeAddedWidgets() {
        viewModelScope.launch {
            widgetRepository.observeAddedWidgets()
                .onStart { _widgetsState.value = UiState.Loading }
                .catch { throwable ->
                    _widgetsState.value = UiState.Error(
                        throwable = throwable,
                        message = throwable.message.orEmpty(),
                    )
                }
                .collect { widgets ->
                    _widgetsState.value = UiState.Data(widgets)
                }
        }
    }

    private fun observeDownloadedThemes() {
        viewModelScope.launch {
            themeRepository.observeDownloadedThemes()
                .onStart { _themesState.value = UiState.Loading }
                .catch { throwable ->
                    _themesState.value = UiState.Error(
                        throwable = throwable,
                        message = throwable.message.orEmpty(),
                    )
                }
                .collect { themes ->
                    _themesState.value = UiState.Data(themes)
                }
        }
    }

    private fun observeDownloadedWallpapers() {
        viewModelScope.launch {
            wallpaperRepository.observeDownloadedWallpapers()
                .onStart { _wallpapersState.value = UiState.Loading }
                .catch { throwable ->
                    _wallpapersState.value = UiState.Error(
                        throwable = throwable,
                        message = throwable.message.orEmpty(),
                    )
                }
                .collect { wallpapers ->
                    _wallpapersState.value = UiState.Data(wallpapers)
                }
        }
    }

}
