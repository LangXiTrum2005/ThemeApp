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

package thinklife.prism.wallpaper.widget.theme.data.repository

import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerConfigModel
import thinklife.prism.wallpaper.widget.theme.data.model.ControllerModel

interface ThemeControllerRepository {

    suspend fun getControllerThemes(
        forceRefresh: Boolean = false
    ): Result<List<ControllerModel>>

    suspend fun getTheme(themeId: String): ControllerModel?

    // Blocking: ControlCenterService dựng overlay đồng bộ trong onServiceConnected/onReceive,
    // không có coroutine scope để chờ.
    fun getThemeConfig(themeId: String): ControllerConfigModel?

    fun getActiveThemeId(): String?

    suspend fun isDownloaded(theme: ControllerModel): Boolean

    suspend fun downloadTheme(
        theme: ControllerModel,
        onProgress: (Int) -> Unit,
    ): Result<Unit>

    fun applyTheme(theme: ControllerModel)
}
