package com.wavez.controllerwidget.core.download

import com.wavez.controllerwidget.di.IoDispatcher
import com.wavez.controllerwidget.data.local.wallpaper.WallpaperFilePathProvider
import com.wavez.controllerwidget.data.local.wallpaper.WallpaperStorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperDownloader @Inject constructor(
    private val fileDownloadHelper: FileDownloadHelper,
    private val pathProvider: WallpaperFilePathProvider,
    private val storageManager: WallpaperStorageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Downloads the image at [url] for [wallpaperId] and saves it atomically.
     * Emits [DownloadState] values. Room record insertion is NOT done here — the caller
     * (repository) is responsible for inserting after receiving [DownloadState.Saving].
     */
    fun download(wallpaperId: String, url: String): Flow<DownloadState<File>> = channelFlow {
        send(DownloadState.Idle)

        val tempFile = pathProvider.getTempWallpaperFile(wallpaperId)
        tempFile.parentFile?.mkdirs()
        tempFile.delete()

        fileDownloadHelper.downloadFile(url, tempFile) { send(it) }
            ?.let { error ->
                send(DownloadState.Failure(error))
                return@channelFlow
            }

        try {
            storageManager.moveTempToFinal(wallpaperId)
        } catch (e: Throwable) {
            tempFile.delete()
            send(DownloadState.Failure(DownloadError.MoveError(e)))
            return@channelFlow
        }

        send(DownloadState.Saving)
    }.flowOn(ioDispatcher)
}
