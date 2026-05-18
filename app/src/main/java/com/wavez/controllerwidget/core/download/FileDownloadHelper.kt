package com.wavez.controllerwidget.core.download

import java.io.File
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared download helper used by all three feature downloaders
 * (Wallpaper, Theme/Controller, IconPack) to avoid repeating exception mapping.
 *
 * [downloadFile] — raw download only (used by Wallpaper: single image file).
 * [downloadAndValidateZip] — download + zip integrity check (used by Theme & IconPack).
 */
@Singleton
class FileDownloadHelper @Inject constructor(
    private val fileDownloader: FileDownloader,
) {
    /**
     * Downloads [url] into [tempFile], mapping all download exceptions to [DownloadError].
     * Calls [onProgress] for each progress update.
     *
     * Returns `null` on success. Returns the [DownloadError] and deletes [tempFile] on failure.
     */
    suspend fun downloadFile(
        url: String,
        tempFile: File,
        onProgress: suspend (DownloadState.Downloading) -> Unit,
    ): DownloadError? {
        return try {
            fileDownloader.download(url, tempFile) { percent ->
                onProgress(DownloadState.Downloading(percent))
            }
            null
        } catch (e: NetworkDownloadException) {
            tempFile.delete()
            DownloadError.NetworkError(e)
        } catch (e: FileWriteDownloadException) {
            tempFile.delete()
            DownloadError.FileWriteError(e)
        } catch (e: EmptyFileDownloadException) {
            tempFile.delete()
            DownloadError.EmptyFile
        } catch (e: Throwable) {
            tempFile.delete()
            DownloadError.UnknownError(e)
        }
    }

    /**
     * Downloads [url] into [tempFile] then verifies it is a readable zip file.
     * Returns `null` on success. Returns the [DownloadError] and deletes [tempFile] on failure.
     */
    suspend fun downloadAndValidateZip(
        url: String,
        tempFile: File,
        onProgress: suspend (DownloadState.Downloading) -> Unit,
    ): DownloadError? {
        downloadFile(url, tempFile, onProgress)?.let { return it }

        val isValidZip = try {
            ZipInputStream(tempFile.inputStream()).use { it.nextEntry != null }
        } catch (_: ZipException) {
            false
        }
        if (!isValidZip) {
            tempFile.delete()
            return DownloadError.InvalidZip
        }
        return null
    }
}
