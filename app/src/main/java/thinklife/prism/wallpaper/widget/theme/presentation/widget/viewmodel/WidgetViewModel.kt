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
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val widgetRepository: WidgetRepository,
) : ViewModel() {

    private val _categoriesState =
        MutableStateFlow<UiState<List<WidgetCategoryModel>>>(UiState.Idle)
    val categoriesState: StateFlow<UiState<List<WidgetCategoryModel>>> =
        _categoriesState.asStateFlow()

    init {
        getCategories()
    }

    fun getCategories(forceRefresh: Boolean = false) {
        if (_categoriesState.value is UiState.Loading) return
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading
            widgetRepository.getCategories(forceRefresh)
                .onSuccess { categories ->
                    _categoriesState.value = UiState.Data(categories)
                }
                .onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _categoriesState.value = UiState.Error(error, message)
                }
        }
    }
}
