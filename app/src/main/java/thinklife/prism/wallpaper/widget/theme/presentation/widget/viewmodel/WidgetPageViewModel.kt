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

package thinklife.prism.wallpaper.widget.theme.presentation.widget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetsModel
import thinklife.prism.wallpaper.widget.theme.data.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetPageViewModel @Inject constructor(
    private val widgetRepository: WidgetRepository,
) : ViewModel() {

    private val _widgetsState = MutableStateFlow<UiState<List<WidgetsModel>>>(UiState.Idle)
    val widgetsState: StateFlow<UiState<List<WidgetsModel>>> = _widgetsState.asStateFlow()

    fun getWidgetsByCategory(categoryId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _widgetsState.value = UiState.Loading
            widgetRepository.getWidgetsByCategory(categoryId, forceRefresh)
                .onSuccess { widgets ->
                    _widgetsState.value = UiState.Data(widgets)
                }
                .onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _widgetsState.value = UiState.Error(error, message)
                }
        }
    }
}
