package com.wavez.controllerwidget.core.download

import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.data.local.iconpack.IconPackFilePathProvider
import com.wavez.controllerwidget.data.local.iconpack.IconPackStorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.zip.ZipException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPackDownloader @Inject constructor(
    private val fileDownloadHelper: FileDownloadHelper,
    private val pathProvider: IconPackFilePathProvider,
    private val storageManager: IconPackStorageManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Downloads the zip at [url] for [iconPackId], validates it, then atomically extracts it.
     * Emits [DownloadState.Saving] when done — caller (repository) emits Success after that.
     * The temp zip is deleted after extraction; manifest.json is the installed marker.
     */
    fun download(iconPackId: String, url: String): Flow<DownloadState<File>> = flow {
        emit(DownloadState.Idle)

        val tempZip = pathProvider.getTempZipFile(iconPackId)
        tempZip.parentFile?.mkdirs()
        tempZip.delete()

        // --- Step 1+2: download zip to temp file + validate ---
        fileDownloadHelper.downloadAndValidateZip(url, tempZip) { emit(it) }
            ?.let { error ->
                emit(DownloadState.Failure(error))
                return@flow
            }

        // --- Step 3: atomically extract (zip deleted inside storageManager on success) ---
        emit(DownloadState.Extracting)
        try {
            storageManager.extractAndInstall(iconPackId, tempZip)
        } catch (e: ZipException) {
            tempZip.delete()
            emit(DownloadState.Failure(DownloadError.InvalidZip))
            return@flow
        } catch (e: Throwable) {
            tempZip.delete()
            emit(DownloadState.Failure(DownloadError.ExtractError(e)))
            return@flow
        }

        emit(DownloadState.Saving)
    }.flowOn(ioDispatcher)
}
