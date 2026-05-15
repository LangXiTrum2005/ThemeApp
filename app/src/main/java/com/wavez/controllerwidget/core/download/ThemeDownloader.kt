package com.wavez.controllerwidget.core.download

import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.data.local.theme.ThemeFilePathProvider
import com.wavez.controllerwidget.data.local.theme.ThemeStorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeDownloader @Inject constructor(
    private val fileDownloader: FileDownloader,
    private val pathProvider: ThemeFilePathProvider,
    private val storageManager: ThemeStorageManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Downloads the zip at [url] for [themeId], validates it, then atomically extracts it.
     * Emits [DownloadState] values. Room record insertion is NOT done here — the caller
     * (repository) is responsible for inserting after receiving [DownloadState.Saving].
     * The zip archive is kept on disk after extraction so [checkDownloaded] can verify via file.isFile.
     */
    fun download(themeId: String, url: String): Flow<DownloadState<File>> = flow {
        emit(DownloadState.Idle)

        val tempZip = pathProvider.getTempThemeFile(themeId)
        tempZip.parentFile?.mkdirs()
        tempZip.delete()

        // --- Step 1: download zip to temp file ---
        try {
            fileDownloader.download(url, tempZip) { percent ->
                emit(DownloadState.Downloading(percent))
            }
        } catch (e: NetworkDownloadException) {
            tempZip.delete()
            emit(DownloadState.Failure(DownloadError.NetworkError(e)))
            return@flow
        } catch (e: FileWriteDownloadException) {
            tempZip.delete()
            emit(DownloadState.Failure(DownloadError.FileWriteError(e)))
            return@flow
        } catch (e: EmptyFileDownloadException) {
            tempZip.delete()
            emit(DownloadState.Failure(DownloadError.EmptyFile))
            return@flow
        } catch (e: Throwable) {
            tempZip.delete()
            emit(DownloadState.Failure(DownloadError.UnknownError(e)))
            return@flow
        }

        // --- Step 2: validate the zip is readable ---
        val isValidZip = try {
            ZipInputStream(tempZip.inputStream()).use { zis -> zis.nextEntry != null }
        } catch (_: ZipException) {
            false
        }
        if (!isValidZip) {
            tempZip.delete()
            emit(DownloadState.Failure(DownloadError.InvalidZip))
            return@flow
        }

        // --- Step 3: move temp zip → final zip ---
        val finalZip = pathProvider.getThemeFile(themeId)
        finalZip.parentFile?.mkdirs()
        if (finalZip.exists() && !finalZip.delete()) {
            tempZip.delete()
            emit(DownloadState.Failure(DownloadError.MoveError(IllegalStateException("Cannot replace existing zip: ${finalZip.absolutePath}"))))
            return@flow
        }
        if (!tempZip.renameTo(finalZip)) {
            tempZip.delete()
            emit(DownloadState.Failure(DownloadError.MoveError(IllegalStateException("Cannot move temp zip to: ${finalZip.absolutePath}"))))
            return@flow
        }

        // --- Step 4: atomically extract zip ---
        emit(DownloadState.Extracting)
        val extractedDir = try {
            storageManager.prepareThemeForApply(themeId)
        } catch (e: ZipException) {
            emit(DownloadState.Failure(DownloadError.InvalidZip))
            return@flow
        } catch (e: Throwable) {
            emit(DownloadState.Failure(DownloadError.ExtractError(e)))
            return@flow
        }

        // Signal to the caller that everything is on disk — Room record should be inserted now.
        // The caller (repository) is responsible for emitting Success after the DB write.
        emit(DownloadState.Saving)
    }.flowOn(ioDispatcher)
}
