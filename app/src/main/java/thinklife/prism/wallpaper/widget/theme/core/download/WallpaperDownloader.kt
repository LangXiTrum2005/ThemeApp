/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.core.download

import thinklife.prism.wallpaper.widget.theme.di.IoDispatcher
import thinklife.prism.wallpaper.widget.theme.data.local.wallpaper.WallpaperFilePathProvider
import thinklife.prism.wallpaper.widget.theme.data.local.wallpaper.WallpaperStorageManager
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
