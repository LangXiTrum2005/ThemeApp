package com.wavez.controllerwidget.core.icons

import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.data.local.iconpack.InstalledAppProvider
import com.wavez.controllerwidget.data.model.IconMappingItem
import com.wavez.controllerwidget.data.model.LocalIconPack
import com.wavez.controllerwidget.data.model.ManualIconMapping
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BuildIconMappingUseCase @Inject constructor(
    private val installedAppProvider: InstalledAppProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Builds the full [IconMappingItem] list for a [LocalIconPack].
     *
     * Matching priority:
     *  1. [manualMappings] override everything.
     *  2. [IconManifestItem.defaultPackageName] is auto-matched against installed apps.
     *  3. Unmatched icons are still returned with all mapping fields null.
     *
     * All icons in the manifest are returned — none are filtered out.
     */
    suspend operator fun invoke(
        localIconPack: LocalIconPack,
        manualMappings: List<ManualIconMapping> = emptyList(),
    ): List<IconMappingItem> = withContext(ioDispatcher) {
        val installedApps = installedAppProvider.getInstalledAppsMap()
        val manualMap = manualMappings.associateBy { it.iconId }

        localIconPack.manifest.icons.map { item ->
            val iconPath = localIconPack.rootDir.resolve(item.iconFile).canonicalPath
            val manual = manualMap[item.iconId]
            if (manual != null) {
                IconMappingItem(
                    iconId = item.iconId,
                    displayName = item.displayName,
                    iconPath = iconPath,
                    defaultPackageName = item.defaultPackageName,
                    mappedPackageName = manual.packageName,
                    mappedAppName = manual.appName,
                    isInstalledMatched = installedApps.containsKey(manual.packageName),
                    isManualMapped = true,
                )
            } else {
                val matched = item.defaultPackageName?.let { installedApps[it] }
                IconMappingItem(
                    iconId = item.iconId,
                    displayName = item.displayName,
                    iconPath = iconPath,
                    defaultPackageName = item.defaultPackageName,
                    mappedPackageName = matched?.packageName,
                    mappedAppName = matched?.appName,
                    isInstalledMatched = matched != null,
                    isManualMapped = false,
                )
            }
        }
    }
}
