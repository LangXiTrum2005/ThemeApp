package com.wavez.controllerwidget.core.download

import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.data.local.wallpaper.WallpaperFilePathProvider
import com.wavez.controllerwidget.data.local.wallpaper.WallpaperStorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperDownloader @Inject constructor(
    private val fileDownloader: FileDownloader,
    private val pathProvider: WallpaperFilePathProvider,
    private val storageManager: WallpaperStorageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Downloads the PNG at [url] for [wallpaperId] and saves it atomically.
     * Emits [DownloadState] values. Room record insertion is NOT done here — the caller
     * (repository) is responsible for inserting after receiving [DownloadState.Saving].
     */
    fun download(wallpaperId: String, url: String): Flow<DownloadState<File>> = flow {
        emit(DownloadState.Idle)

        val tempFile = pathProvider.getTempWallpaperFile(wallpaperId)
        tempFile.parentFile?.mkdirs()
        tempFile.delete()

        // --- Step 1: download PNG to temp file ---
        try {
            fileDownloader.download(url, tempFile) { percent ->
                emit(DownloadState.Downloading(percent))
            }
        } catch (e: NetworkDownloadException) {
            tempFile.delete()
            emit(DownloadState.Failure(DownloadError.NetworkError(e)))
            return@flow
        } catch (e: FileWriteDownloadException) {
            tempFile.delete()
            emit(DownloadState.Failure(DownloadError.FileWriteError(e)))
            return@flow
        } catch (e: EmptyFileDownloadException) {
            tempFile.delete()
            emit(DownloadState.Failure(DownloadError.EmptyFile))
            return@flow
        } catch (e: Throwable) {
            tempFile.delete()
            emit(DownloadState.Failure(DownloadError.UnknownError(e)))
            return@flow
        }

        // --- Step 2: atomically move temp → final ---
        val finalFile = try {
            storageManager.moveTempToFinal(wallpaperId)
        } catch (e: Throwable) {
            tempFile.delete()
            emit(DownloadState.Failure(DownloadError.MoveError(e)))
            return@flow
        }

        // Signal to the caller that the file is on disk — Room record should be inserted now.
        // The caller (repository) is responsible for emitting Success after the DB write.
        emit(DownloadState.Saving)
    }.flowOn(ioDispatcher)
}
