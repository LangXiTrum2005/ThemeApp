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
class AppliedThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val _suggestedThemes = MutableStateFlow<UiState<List<ThemeModel>>>(UiState.Idle)
    val suggestedThemes: StateFlow<UiState<List<ThemeModel>>> = _suggestedThemes.asStateFlow()

    fun loadSuggestedThemes() {
        viewModelScope.launch {
            _suggestedThemes.value = UiState.Loading
            themeRepository.getThemes()
                .onSuccess { themes -> _suggestedThemes.value = UiState.Data(themes) }
                .onFailure { throwable ->
                    _suggestedThemes.value = UiState.Error(
                        throwable = throwable,
                        message = throwable.message.orEmpty(),
                    )
                }
        }
    }
}
