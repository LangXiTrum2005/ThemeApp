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

package thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.data.model.wallpaper.WallpaperModel
import thinklife.prism.wallpaper.widget.theme.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperPageViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository
) : ViewModel() {
    private val _wallpapersState =
        MutableStateFlow<UiState<List<WallpaperModel>>>(UiState.Idle)
    val wallpapersState: StateFlow<UiState<List<WallpaperModel>>> =
        _wallpapersState.asStateFlow()

    fun getWallpapersByCategory(categoryId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _wallpapersState.value = UiState.Loading
            wallpaperRepository.getWallpapersByCategory(categoryId, forceRefresh)
                .onSuccess { wallpapers ->
                    _wallpapersState.value = UiState.Data(wallpapers)
                }.onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _wallpapersState.value = UiState.Error(error, message)
                }
        }
    }

}