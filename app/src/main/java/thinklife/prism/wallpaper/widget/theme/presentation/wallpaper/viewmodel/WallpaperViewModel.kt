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
import thinklife.prism.wallpaper.widget.theme.data.model.WallpaperCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
) : ViewModel() {

    private val _categoriesResult =
        MutableStateFlow<UiState<List<WallpaperCategoryModel>>>(UiState.Idle)
    val categoriesResult: StateFlow<UiState<List<WallpaperCategoryModel>>> = _categoriesResult

    init {
        getCategories()
    }

    fun getCategories() {
        if (_categoriesResult.value is UiState.Loading) return
        viewModelScope.launch {
            _categoriesResult.value = UiState.Loading
            val categories = wallpaperRepository.getCategoriesUnlimited()
            categories.onSuccess {
                _categoriesResult.value = UiState.Data(it)
            }.onFailure {
                val message = it.message.orEmpty().ifBlank { "Unknown error" }
                _categoriesResult.value = UiState.Error(it, message)
            }
        }
    }

}
