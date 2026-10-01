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

package thinklife.prism.wallpaper.widget.theme.presentation.iconpack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconPackModel
import thinklife.prism.wallpaper.widget.theme.data.repository.IconPackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplyIconsViewModel @Inject constructor(
    private val iconPackRepository: IconPackRepository
) : ViewModel() {

    private val _iconsState =
        MutableStateFlow<UiState<List<IconPackModel>>>(UiState.Idle)
    val iconsState: StateFlow<UiState<List<IconPackModel>>> =
        _iconsState.asStateFlow()

    var currentIconsId: String? = null

    fun getIcons() {
        viewModelScope.launch(Dispatchers.IO) {
            _iconsState.value = UiState.Loading
            iconPackRepository.getIconPacks()
                .onSuccess { wallpapers ->
                    val filtered = wallpapers.filter {
                        it.id != currentIconsId
                    }.take(10)

                    _iconsState.value = UiState.Data(filtered)
                }.onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _iconsState.value = UiState.Error(error, message)
                }
        }
    }

}