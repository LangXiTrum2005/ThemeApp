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

package thinklife.prism.wallpaper.widget.theme.presentation.iconpack.mapper

import thinklife.prism.wallpaper.widget.theme.data.model.AppInfo
import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconItemModel
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.model.IconPackItemUi
import javax.inject.Inject

class IconPackItemMapper @Inject constructor() {

    fun map(
        icon: IconItemModel,
        installedAppsByPackage: Map<String, AppInfo>,
        installedAppsByName: Map<String, AppInfo>,
        manualMapping: AppInfo? = null,
        isApplied: Boolean = false,
    ): IconPackItemUi {
        val packageName = icon.packageName.takeIf { it.isNotBlank() }
        val iconId = stableId(icon)
        val installedApp = manualMapping ?: findInstalledApp(
            icon = icon,
            packageName = packageName,
            installedAppsByPackage = installedAppsByPackage,
            installedAppsByName = installedAppsByName,
        )
        return IconPackItemUi(
            id = iconId,
            packageName = packageName,
            appName = icon.appName,
            iconUrl = icon.iconUrl,
            installedApp = installedApp,
            isInstalled = packageName != null && installedAppsByPackage.containsKey(packageName),
            isMapped = installedApp != null,
            selectedPackageName = manualMapping?.packageName,
            selectedAppName = manualMapping?.appName,
            selectedAppIcon = manualMapping?.icon,
            isManualMappingRequired = packageName == null,
            isApplied = isApplied,
        )
    }

    fun stableId(icon: IconItemModel): Long {
        return listOf(icon.packageName, icon.appName, icon.iconUrl).joinToString("|").hashCode().toLong()
    }

    private fun findInstalledApp(
        icon: IconItemModel,
        packageName: String?,
        installedAppsByPackage: Map<String, AppInfo>,
        installedAppsByName: Map<String, AppInfo>,
    ): AppInfo? {
        packageName?.let { installedAppsByPackage[it] }?.let { return it }
        if (packageName != null) return null
        return installedAppsByName[icon.appName.normalizedAppName()]
    }

    companion object {
        fun String.normalizedAppName(): String {
            return lowercase().filter { it.isLetterOrDigit() }
        }
    }
}
