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

package thinklife.prism.wallpaper.widget.theme.presentation.controller.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import thinklife.prism.wallpaper.widget.theme.core.base.BaseViewModel
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerTheme
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.toControllerTheme
import thinklife.prism.wallpaper.widget.theme.core.permission.ControllerPermissionChecker
import thinklife.prism.wallpaper.widget.theme.data.repository.ThemeControllerRepository
import javax.inject.Inject

@HiltViewModel
class ControllerViewModel @Inject constructor(
    private val controllerRepository: ThemeControllerRepository,
    private val permissionChecker: ControllerPermissionChecker,
) : BaseViewModel() {

    private val _showEnableControllerCard = MutableStateFlow(!permissionChecker.isControllerEnabled())
    val showEnableControllerCard: StateFlow<Boolean> = _showEnableControllerCard.asStateFlow()

    private val _themesState = MutableStateFlow<UiState<List<ControllerTheme>>>(UiState.Idle)
    val themesState: StateFlow<UiState<List<ControllerTheme>>> = _themesState.asStateFlow()

    private val _activeTheme = MutableStateFlow<ControllerTheme?>(null)
    val activeTheme: StateFlow<ControllerTheme?> = _activeTheme.asStateFlow()

    fun refreshPermissionState() {
        // Quyền có thể bị thu hồi trong Settings, nên không dùng cờ hoàn thành đã lưu.
        _showEnableControllerCard.value = !permissionChecker.isControllerEnabled()
    }

    fun loadThemesIfNeeded() {
        if (_themesState.value is UiState.Idle) loadThemes()
    }

    fun loadThemes(forceRefresh: Boolean = false) {
        launchSafely(onError = { error ->
            _themesState.value = UiState.Error(error, error.message.orEmpty())
        }) {
            _themesState.value = UiState.Loading
            controllerRepository.getControllerThemes(forceRefresh).onSuccess { themes ->
                _themesState.value = UiState.Data(themes.map { it.toControllerTheme() })
                refreshActiveTheme()
            }.onFailure { error ->
                val message = error.message.orEmpty()
                _themesState.value = UiState.Error(error, message)
            }
        }
    }

    // Gọi lại mỗi lần màn resume vì theme có thể vừa được áp dụng ở màn preview.
    fun refreshActiveTheme() {
        launchSafely {
            _activeTheme.value =
                controllerRepository.getActiveThemeId()?.let { controllerRepository.getTheme(it) }
                    ?.toControllerTheme()
        }
    }
}
