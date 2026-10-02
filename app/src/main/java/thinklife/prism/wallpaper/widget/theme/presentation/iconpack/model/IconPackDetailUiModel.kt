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

package thinklife.prism.wallpaper.widget.theme.presentation.iconpack.model

import android.graphics.drawable.Drawable
import thinklife.prism.wallpaper.widget.theme.data.model.AppInfo

data class IconPackItemUi(
    val id: Long,
    val packageName: String?,
    val appName: String,
    val iconUrl: String,
    val installedApp: AppInfo?,
    val isInstalled: Boolean,
    val isMapped: Boolean,
    val selectedPackageName: String? = null,
    val selectedAppName: String? = null,
    val selectedAppIcon: Drawable? = null,
    val isManualMappingRequired: Boolean,
    val isApplied: Boolean = false,
) {
    val targetPackageName: String? get() = selectedPackageName ?: installedApp?.packageName
    val targetLabel: String get() = selectedAppName ?: installedApp?.appName ?: appName
    val targetIcon: Drawable? get() = selectedAppIcon ?: installedApp?.icon
    val isInstallable: Boolean get() = targetPackageName != null
    val canInstall: Boolean get() = isInstallable
}
