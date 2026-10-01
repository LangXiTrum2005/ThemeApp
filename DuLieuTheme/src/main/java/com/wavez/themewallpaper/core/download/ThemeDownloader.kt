package com.wavez.themewallpaper.core.download

import com.wavez.themewallpaper.di.IoDispatcher
import com.wavez.themewallpaper.data.local.controller.ControllerFilePathProvider
import com.wavez.themewallpaper.data.local.controller.ControllerStorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.zip.ZipException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeDownloader @Inject constructor(
    private val fileDownloadHelper: FileDownloadHelper,
    private val pathProvider: ControllerFilePathProvider,
    private val storageManager: ControllerStorageManager,
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

        // --- Step 1+2: download zip to temp file + validate ---
        fileDownloadHelper.downloadAndValidateZip(url, tempZip) { emit(it) }
            ?.let { error ->
                emit(DownloadState.Failure(error))
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
