package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.core.download.WallpaperDownloader
import com.wavez.controllerwidget.data.local.dao.DownloadedWallpaperDao
import com.wavez.controllerwidget.data.local.entity.DownloadedWallpaperEntity
import com.wavez.controllerwidget.data.local.wallpaper.WallpaperStorageManager
import com.wavez.controllerwidget.data.model.DownloadedResInfo
import com.wavez.controllerwidget.data.model.WallpaperCategoryModel
import com.wavez.controllerwidget.data.model.WallpaperModel
import com.wavez.controllerwidget.data.network.api.ControllerWidgetApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class WallpaperRepositoryImpl @Inject constructor(
    private val apiService: ControllerWidgetApiService,
    private val wallpaperDownloader: WallpaperDownloader,
    private val wallpaperStorageManager: WallpaperStorageManager,
    private val downloadedWallpaperDao: DownloadedWallpaperDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : WallpaperRepository {

    private var cachedCategories: List<WallpaperCategoryModel>? = null
    private var cachedWallpapers: List<WallpaperModel>? = null

    override suspend fun getCategoriesUnlimited(
        forceRefresh: Boolean
    ): Result<List<WallpaperCategoryModel>> {
        if (!forceRefresh) {
            cachedCategories?.let { return Result.success(it) }
        }

        return repositoryRequest { apiService.getCategoriesUnlimited() }
            .onSuccess { cachedCategories = it }
    }

    override suspend fun getAllWallpapersUnlimited(
        forceRefresh: Boolean
    ): Result<List<WallpaperModel>> {
        if (!forceRefresh) {
            cachedWallpapers?.let { return Result.success(it) }
        }

        return repositoryRequest { apiService.getAllWallpapersUnlimited() }
            .onSuccess { cachedWallpapers = it }
    }

    override fun downloadWallpaper(wallpaper: WallpaperModel): Flow<DownloadState<File>> = flow {
        // Short-circuit: already fully downloaded
        if (checkWallpaperDownloaded(wallpaper.id)) {
            val localFile = wallpaperStorageManager.getWallpaperFile(wallpaper.id)
            emit(DownloadState.Success(localFile))
            return@flow
        }
        // Remove any orphaned DB record before starting fresh
        downloadedWallpaperDao.deleteByWallpaperId(wallpaper.id)

        wallpaperDownloader.download(wallpaper.id, wallpaper.url).collect { state ->
            when (state) {
                is DownloadState.Saving -> {
                    emit(DownloadState.Saving)
                    // File is fully on disk — persist Room record
                    val localFile = wallpaperStorageManager.getWallpaperFile(wallpaper.id)
                    downloadedWallpaperDao.upsert(
                        DownloadedWallpaperEntity(
                            wallpaperId = wallpaper.id,
                            filePath = localFile.absolutePath,
                            downloadedAt = System.currentTimeMillis(),
                            fileSize = localFile.length(),
                        )
                    )
                    // Fire-and-forget stats update; failure must not block the download result
                    runCatching {
                        DownloadedResInfo.fromResource(wallpaper)
                            ?.let { apiService.updateWallpaperDownloadAmount(it) }
                    }
                    emit(DownloadState.Success(localFile))
                }
                else -> emit(state)
            }
        }
    }.flowOn(ioDispatcher)

    override suspend fun checkWallpaperDownloaded(wallpaperId: String): Boolean =
        withContext(ioDispatcher) {
            val entity = downloadedWallpaperDao.getByWallpaperId(wallpaperId)
                ?: return@withContext false
            val file = File(entity.filePath)
            if (file.isFile) {
                true
            } else {
                downloadedWallpaperDao.deleteByWallpaperId(wallpaperId)
                false
            }
        }

    override suspend fun getLocalWallpaperFile(wallpaperId: String): File? =
        withContext(ioDispatcher) {
            val entity = downloadedWallpaperDao.getByWallpaperId(wallpaperId)
                ?: return@withContext null
            val file = File(entity.filePath)
            if (file.isFile) {
                file
            } else {
                downloadedWallpaperDao.deleteByWallpaperId(wallpaperId)
                null
            }
        }

    override suspend fun applyWallpaper(wallpaper: WallpaperModel): Result<File> =
        withContext(ioDispatcher) {
            runCatching {
                getLocalWallpaperFile(wallpaper.id)
                    ?: error("Wallpaper is not downloaded: ${wallpaper.id}")
                // TODO: call WallpaperManagerCore in the ViewModel using this File:
                //   val options = WallpaperOptions(source = WallpaperSource.StaticImage(Uri.fromFile(file)))
                //   wallpaperManagerCore.applyWallpaper(options).collect { ... }
            }
        }
}
