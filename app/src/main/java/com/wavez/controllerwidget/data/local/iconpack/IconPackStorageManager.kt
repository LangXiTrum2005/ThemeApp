package com.wavez.controllerwidget.data.local.iconpack

import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.data.local.unzipSafely
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPackStorageManager @Inject constructor(
    private val pathProvider: IconPackFilePathProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun isInstalled(iconPackId: String): Boolean = withContext(ioDispatcher) {
        pathProvider.getManifestFile(iconPackId).isFile
    }

    /**
     * Atomically extracts [zipFile] into the icon pack directory.
     * Extracts to a sibling temp dir first, then renames to final dir to avoid partial states.
     * Deletes [zipFile] after successful extraction — zip is transient for icon packs.
     */
    suspend fun extractAndInstall(iconPackId: String, zipFile: File): File =
        withContext(ioDispatcher) {
            val targetDir = pathProvider.getIconPackDir(iconPackId)
            val tempDir = File(targetDir.parent, "${targetDir.name}.tmp")
            tempDir.deleteRecursively()
            tempDir.mkdirs()
            try {
                unzipSafely(zipFile, tempDir)
                targetDir.deleteRecursively()
                check(tempDir.renameTo(targetDir)) {
                    "Cannot move extracted dir to: ${targetDir.absolutePath}"
                }
                zipFile.delete()
                targetDir
            } catch (e: Throwable) {
                tempDir.deleteRecursively()
                throw e
            }
        }

    suspend fun deleteIconPack(iconPackId: String) = withContext(ioDispatcher) {
        pathProvider.getIconPackDir(iconPackId).deleteRecursively()
        pathProvider.getTempZipFile(iconPackId).delete()
    }

    suspend fun getIconPackDir(iconPackId: String): File = withContext(ioDispatcher) {
        pathProvider.getIconPackDir(iconPackId)
    }

}
