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

package thinklife.prism.wallpaper.widget.theme.presentation.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeModel
import thinklife.prism.wallpaper.widget.theme.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemePageViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val _themesState = MutableStateFlow<UiState<List<ThemeModel>>>(UiState.Idle)
    val themesState: StateFlow<UiState<List<ThemeModel>>> = _themesState.asStateFlow()

    fun getThemesByCategory(categoryId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _themesState.value = UiState.Loading
            themeRepository.getThemesByCategory(categoryId, forceRefresh)
                .onSuccess { themes ->
                    _themesState.value = UiState.Data(themes)
                }
                .onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _themesState.value = UiState.Error(error, message)
                }
        }
    }
}
