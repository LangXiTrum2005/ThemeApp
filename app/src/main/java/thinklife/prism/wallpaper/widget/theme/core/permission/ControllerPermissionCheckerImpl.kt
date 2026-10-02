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

package thinklife.prism.wallpaper.widget.theme.core.permission

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.PermissionHelper
import javax.inject.Inject

class ControllerPermissionCheckerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ControllerPermissionChecker {

    override fun isControllerEnabled(): Boolean = PermissionHelper.hasAccessibilityEnabled(context)
}
