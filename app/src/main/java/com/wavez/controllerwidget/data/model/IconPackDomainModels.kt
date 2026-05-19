package com.wavez.controllerwidget.data.model

import android.graphics.drawable.Drawable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

@JsonClass(generateAdapter = true)
data class IconManifestItem(
    @param:Json(name = "iconId") val iconId: String,
    @param:Json(name = "displayName") val displayName: String,
    @param:Json(name = "iconFile") val iconFile: String,
    @param:Json(name = "defaultPackageName") val defaultPackageName: String?,
)

@JsonClass(generateAdapter = true)
data class IconPackManifest(
    @param:Json(name = "iconPackId") val iconPackId: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "version") val version: Int,
    @param:Json(name = "icons") val icons: List<IconManifestItem>,
)

data class LocalIconPack(
    val iconPackId: String,
    val version: Int,
    val rootDir: File,
    val manifest: IconPackManifest,
    val manifestFile: File,
)

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
)

data class IconMappingItem(
    val iconId: String,
    val displayName: String,
    val iconPath: String,
    val defaultPackageName: String?,
    val mappedPackageName: String?,
    val mappedAppName: String?,
    val isInstalledMatched: Boolean,
    val isManualMapped: Boolean,
)

@JsonClass(generateAdapter = true)
data class ManualIconMapping(
    @param:Json(name = "iconPackId") val iconPackId: String,
    @param:Json(name = "iconId") val iconId: String,
    @param:Json(name = "packageName") val packageName: String,
    @param:Json(name = "appName") val appName: String,
)
