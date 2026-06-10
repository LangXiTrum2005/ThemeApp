package com.wavez.themewallpaper.ui.iconpack.mapper

import com.wavez.themewallpaper.data.model.AppInfo
import com.wavez.themewallpaper.data.model.icon.IconItemModel
import com.wavez.themewallpaper.data.repository.SharedPref
import com.wavez.themewallpaper.ui.iconpack.model.IconPackItemUi
import javax.inject.Inject

class IconPackItemMapper @Inject constructor(
    private val sharedPref: SharedPref,
) {

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
            isUnlocked = sharedPref.isVip || sharedPref.isIconUnlocked(iconId.toString()),
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
