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

package thinklife.prism.wallpaper.widget.theme.presentation.icons.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconPackModel
import thinklife.prism.wallpaper.widget.theme.data.repository.IconPackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IconsViewModel @Inject constructor(
    private val iconPackRepository: IconPackRepository
) : ViewModel() {

    private val _iconsResult =
        MutableStateFlow<UiState<List<IconPackModel>>>(UiState.Idle)
    val iconResult: StateFlow<UiState<List<IconPackModel>>> = _iconsResult

    fun getIcons() {
        if (_iconsResult.value is UiState.Loading) return
        viewModelScope.launch {
            _iconsResult.value = UiState.Loading
            val icons = iconPackRepository.getIconPacks()
            icons.onSuccess {
                _iconsResult.value = UiState.Data(it)
            }.onFailure {
                val message = it.message.orEmpty().ifBlank { "Unknown error" }
                _iconsResult.value = UiState.Error(it, message)
            }
        }
    }

}