package com.wavez.controllerwidget.data.local.iconpack

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.wavez.controllerwidget.data.model.IconPackManifest
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPackManifestParser @Inject constructor(
    private val moshi: Moshi,
) {
    @OptIn(ExperimentalStdlibApi::class)
    fun parse(manifestFile: File): IconPackManifest {
        require(manifestFile.isFile) {
            "Manifest file does not exist: ${manifestFile.absolutePath}"
        }
        val json = manifestFile.readText()
        require(json.isNotBlank()) {
            "Manifest file is empty: ${manifestFile.absolutePath}"
        }
        return moshi.adapter<IconPackManifest>().fromJson(json)
            ?: error("Moshi returned null for manifest: ${manifestFile.absolutePath}")
    }
}
