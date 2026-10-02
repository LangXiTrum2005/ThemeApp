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

import androidx.annotation.StringRes
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerTheme
data class ControllerPreviewUiState(
    val theme: ControllerTheme? = null,
    val downloaded: Boolean = false,
    val active: Boolean = false,
    val downloading: Boolean = false,
    val progress: Int = 0,
) {
    val actionEnabled: Boolean
        get() = theme != null && !downloading && !(downloaded && active)

    @get:StringRes
    val actionLabelRes: Int
        get() = when {
            theme == null -> R.string.loading
            downloading -> R.string.controller_preview_downloading
            downloaded && active -> R.string.controller_theme_active
            downloaded -> R.string.controller_preview_apply_action
            else -> R.string.controller_preview_download_action
        }

    @get:StringRes
    val statusLabelRes: Int
        get() = when {
            theme == null -> R.string.controller_preview_loading_details
            downloading -> R.string.controller_preview_downloading_hint
            downloaded && active -> R.string.controller_preview_applied_hint
            downloaded -> R.string.controller_preview_ready_hint
            else -> R.string.controller_preview_download_hint
        }
}

sealed interface ControllerPreviewEvent {
    data object NotFound : ControllerPreviewEvent
    data object DownloadFailed : ControllerPreviewEvent
    data class Applied(val themeName: String) : ControllerPreviewEvent
}
