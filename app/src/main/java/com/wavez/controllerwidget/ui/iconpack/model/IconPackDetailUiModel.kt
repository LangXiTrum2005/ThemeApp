package com.wavez.controllerwidget.ui.iconpack.model

import android.graphics.drawable.Drawable
import com.wavez.controllerwidget.data.model.AppInfo

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
    val isUnlocked: Boolean,
) {
    val targetPackageName: String? get() = selectedPackageName ?: installedApp?.packageName
    val targetLabel: String get() = selectedAppName ?: installedApp?.appName ?: appName
    val targetIcon: Drawable? get() = selectedAppIcon ?: installedApp?.icon
    val isInstallable: Boolean get() = targetPackageName != null
    val canInstall: Boolean get() = isInstallable && isUnlocked
}
