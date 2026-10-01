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

package thinklife.prism.wallpaper.widget.theme.presentation.controller.model

import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerTheme
data class ControllerPreviewUiState(
    val theme: ControllerTheme? = null,
    val downloaded: Boolean = false,
    val active: Boolean = false,
    val downloading: Boolean = false,
    val progress: Int = 0,
)

sealed interface ControllerPreviewEvent {
    data object NotFound : ControllerPreviewEvent
    data object DownloadFailed : ControllerPreviewEvent
    data class Applied(val themeName: String) : ControllerPreviewEvent
}
