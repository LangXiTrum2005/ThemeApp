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

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.toControllerTheme
import thinklife.prism.wallpaper.widget.theme.data.model.ControllerModel
import thinklife.prism.wallpaper.widget.theme.data.repository.ThemeControllerRepository
import thinklife.prism.wallpaper.widget.theme.presentation.controller.model.ControllerPreviewEvent
import thinklife.prism.wallpaper.widget.theme.presentation.controller.model.ControllerPreviewUiState
import javax.inject.Inject

@HiltViewModel
class ControllerThemePreviewViewModel @Inject constructor(
    private val controllerRepository: ThemeControllerRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "ControllerThemePreview"
    }

    private val _uiState = MutableStateFlow(ControllerPreviewUiState())
    val uiState: StateFlow<ControllerPreviewUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ControllerPreviewEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ControllerPreviewEvent> = _events.asSharedFlow()

    private var model: ControllerModel? = null

    fun load(themeId: String) {
        if (model != null) return
        viewModelScope.launch {
            val theme = controllerRepository.getTheme(themeId) ?: run {
                _events.emit(ControllerPreviewEvent.NotFound)
                return@launch
            }
            model = theme
            _uiState.value = ControllerPreviewUiState(
                theme = theme.toControllerTheme(),
                downloaded = controllerRepository.isDownloaded(theme),
                active = controllerRepository.getActiveThemeId() == theme.id,
            )
        }
    }

    fun onActionClick() {
        val theme = model ?: return
        val state = _uiState.value
        if (state.downloading) return
        if (state.downloaded) {
            controllerRepository.applyTheme(theme)
            _uiState.update { it.copy(active = true) }
            _events.tryEmit(ControllerPreviewEvent.Applied(state.theme?.name.orEmpty()))
        } else {
            download(theme)
        }
    }

    private fun download(theme: ControllerModel) {
        _uiState.update { it.copy(downloading = true, progress = 0) }
        viewModelScope.launch {
            val result = controllerRepository.downloadTheme(theme) { progress ->
                _uiState.update { it.copy(progress = progress) }
            }
            val downloaded = controllerRepository.isDownloaded(theme)
            _uiState.update { it.copy(downloading = false, downloaded = downloaded) }
            result.onFailure { error ->
                Log.w(TAG, "Theme download failed: ${theme.id}", error)
                _events.emit(ControllerPreviewEvent.DownloadFailed)
            }
        }
    }
}
