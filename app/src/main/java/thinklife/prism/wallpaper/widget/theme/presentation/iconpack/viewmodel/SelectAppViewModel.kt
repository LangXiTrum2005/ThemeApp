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
import thinklife.prism.wallpaper.widget.theme.data.local.InstalledAppProvider
import thinklife.prism.wallpaper.widget.theme.data.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectAppViewModel @Inject constructor(
    private val provider: InstalledAppProvider,
) : ViewModel() {

    private val _apps = MutableStateFlow<UiState<List<AppInfo>>>(UiState.Idle)
    val apps: StateFlow<UiState<List<AppInfo>>> = _apps.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (_apps.value is UiState.Data) return
        viewModelScope.launch {
            _apps.value = UiState.Loading
            runCatching { provider.getInstalledApps() }
                .onSuccess { _apps.value = UiState.Data(it) }
                .onFailure { t ->
                    _apps.value = UiState.Error(t, t.message.orEmpty().ifBlank { "Failed to load apps" })
                }
        }
    }
}
