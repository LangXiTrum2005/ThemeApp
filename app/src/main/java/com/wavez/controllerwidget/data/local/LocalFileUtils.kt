package com.wavez.controllerwidget.data.local

import java.io.File
import java.util.zip.ZipInputStream

/**
 * Sanitizes a string for use as a file/directory name.
 * Replaces any character that is not alphanumeric, dot, underscore, or hyphen with underscore.
 */
internal fun sanitize(value: String): String =
    value.replace(Regex("[^A-Za-z0-9._-]"), "_")

/**
 * Extracts [zipFile] into [targetDir] with zip-slip protection.
 * Throws [IllegalStateException] if a zip entry tries to escape [targetDir].
 */
internal fun unzipSafely(zipFile: File, targetDir: File) {
    ZipInputStream(zipFile.inputStream().buffered()).use { zip ->
        while (true) {
            val entry = zip.nextEntry ?: break
            val outFile = File(targetDir, entry.name)
            val canonicalTarget = targetDir.canonicalPath + File.separator
            if (!outFile.canonicalPath.startsWith(canonicalTarget)) {
                error("Invalid zip entry path (zip-slip): ${entry.name}")
            }
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                outFile.outputStream().use { zip.copyTo(it) }
            }
            zip.closeEntry()
        }
    }
}
