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
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetsModel
import thinklife.prism.wallpaper.widget.theme.data.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetSuccessViewModel @Inject constructor(
    private val widgetRepository: WidgetRepository,
) : ViewModel() {

    private val _suggestedWidgets = MutableStateFlow<List<WidgetsModel>>(emptyList())
    val suggestedWidgets: StateFlow<List<WidgetsModel>> = _suggestedWidgets.asStateFlow()

    fun loadSameCategoryWidgets(categoryId: String, excludeId: String) {
        viewModelScope.launch {
            widgetRepository.getAllWidgets()
                .onSuccess { widgets ->
                    _suggestedWidgets.value = widgets.filter {
                        it.categoryId == categoryId && it.id != excludeId
                    }
                }
        }
    }

    fun saveAddedWidget(widget: WidgetsModel) {
        viewModelScope.launch {
            widgetRepository.saveAddedWidget(widget)
        }
    }
}
