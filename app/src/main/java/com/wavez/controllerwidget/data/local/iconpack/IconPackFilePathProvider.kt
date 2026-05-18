package com.wavez.controllerwidget.data.local.iconpack

import android.content.Context
import com.wavez.controllerwidget.data.local.IconPackStorageConfig
import com.wavez.controllerwidget.data.local.sanitize
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPackFilePathProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getIconPackDir(iconPackId: String): File =
        File(baseDir(), sanitize(iconPackId))

    fun getTempZipFile(iconPackId: String): File =
        File(baseDir(), "${sanitize(iconPackId)}${IconPackStorageConfig.ICON_PACK_TEMP_SUFFIX}")

    fun getManifestFile(iconPackId: String): File =
        File(getIconPackDir(iconPackId), IconPackStorageConfig.ICON_PACK_MANIFEST_FILE)

    private fun baseDir(): File =
        File(context.filesDir, IconPackStorageConfig.ICON_PACK_BASE_DIR)


}
