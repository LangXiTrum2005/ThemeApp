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

package thinklife.prism.wallpaper.widget.theme.presentation.controllertutorial

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import thinklife.prism.wallpaper.widget.theme.core.base.BaseViewModel
import thinklife.prism.wallpaper.widget.theme.core.permission.ControllerPermissionChecker
import javax.inject.Inject

@HiltViewModel
class ControllerTutorialViewModel @Inject constructor(
    private val permissionChecker: ControllerPermissionChecker,
) : BaseViewModel() {

    private val _controllerEnabled = MutableStateFlow(permissionChecker.isControllerEnabled())
    val controllerEnabled = _controllerEnabled.asStateFlow()

    private val _openAccessibilitySettings = MutableSharedFlow<Unit>()
    val openAccessibilitySettings = _openAccessibilitySettings.asSharedFlow()

    fun refreshPermissionState() {
        _controllerEnabled.value = permissionChecker.isControllerEnabled()
    }

    fun onEnableControllerClicked() {
        refreshPermissionState()
        if (!_controllerEnabled.value) {
            launchSafely { _openAccessibilitySettings.emit(Unit) }
        }
    }
}

