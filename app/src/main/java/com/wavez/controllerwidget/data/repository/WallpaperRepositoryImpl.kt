package com.wavez.controllerwidget.data.repository

import android.net.Uri
import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.core.download.WallpaperDownloader
import com.wavez.controllerwidget.core.wallpaper.WallpaperManagerCore
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperOptions
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperResult
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperSource
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperTarget
import com.wavez.controllerwidget.data.local.dao.DownloadedWallpaperDao
import com.wavez.controllerwidget.data.local.entity.DownloadedWallpaperEntity
import com.wavez.controllerwidget.data.local.wallpaper.WallpaperStorageManager
import com.wavez.controllerwidget.data.model.DownloadedResInfo
import com.wavez.controllerwidget.data.model.WallpaperCategoryModel
import com.wavez.controllerwidget.data.model.WallpaperModel
import com.wavez.controllerwidget.data.model.WallpaperType
import com.wavez.controllerwidget.data.model.wallpaperType
import com.wavez.controllerwidget.data.network.api.ControllerWidgetApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

class WallpaperRepositoryImpl @Inject constructor(
    private val apiService: ControllerWidgetApiService,
    private val wallpaperDownloader: WallpaperDownloader,
    private val wallpaperStorageManager: WallpaperStorageManager,
    private val downloadedWallpaperDao: DownloadedWallpaperDao,
    private val wallpaperManagerCore: WallpaperManagerCore,
    private val sharedPref: SharedPref,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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

    override suspend fun getWallpapersByCategory(
        categoryId: String,
        forceRefresh: Boolean
    ): Result<List<WallpaperModel>> {
        val allResult = getAllWallpapersUnlimited(forceRefresh)
        return allResult.map { wallpapers ->
            wallpapers.filter { it.categoryId == categoryId }
        }
    }

    override fun downloadWallpaper(wallpaper: WallpaperModel): Flow<DownloadState<File>> =
        channelFlow {
            // Short-circuit: already fully downloaded
            if (checkWallpaperDownloaded(wallpaper.id)) {
                send(DownloadState.Success(wallpaperStorageManager.getWallpaperFile(wallpaper.id)))
                return@channelFlow
            }
            // Remove any orphaned DB record before starting fresh
            downloadedWallpaperDao.deleteByWallpaperId(wallpaper.id)

            wallpaperDownloader.download(wallpaper.id, wallpaper.url).collect { state ->
                when (state) {
                    is DownloadState.Saving -> {
                        send(DownloadState.Saving)
                        val localFile = wallpaperStorageManager.getWallpaperFile(wallpaper.id)
                        downloadedWallpaperDao.upsert(
                            DownloadedWallpaperEntity(
                                wallpaperId = wallpaper.id,
                                filePath = localFile.absolutePath,
                                downloadedAt = System.currentTimeMillis(),
                                fileSize = localFile.length(),
                            )
                        )
                        runCatching {
                            DownloadedResInfo.fromResource(wallpaper)
                                ?.let { apiService.updateWallpaperDownloadAmount(it) }
                        }
                        send(DownloadState.Success(localFile))
                    }

                    else -> send(state)
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

    override fun canDownloadWallpaper(wallpaper: WallpaperModel): Boolean {
        return wallpaper.isFree ||
            sharedPref.isVip ||
            sharedPref.isWallpaperUnlocked(wallpaper.id)
    }

    override fun unlockWallpaper(wallpaper: WallpaperModel) {
        sharedPref.unlockWallpaper(wallpaper.id)
    }

    override suspend fun applyWallpaper(
        wallpaper: WallpaperModel,
        wallpaperTarget: WallpaperTarget
    ): Result<Unit> =
        runCatching {
            val source: WallpaperSource = when (wallpaper.wallpaperType) {
                WallpaperType.IMAGE -> WallpaperSource.OnlineImage(wallpaper.url)
                WallpaperType.GIF -> WallpaperSource.Gif(Uri.fromFile(ensureDownloaded(wallpaper)))
                WallpaperType.VIDEO -> WallpaperSource.Video(Uri.fromFile(ensureDownloaded(wallpaper)))
            }
            wallpaperManagerCore.applyWallpaper(
                WallpaperOptions(
                    source = source,
                    target = wallpaperTarget
                )
            )
                .collect { result ->
                    if (result is WallpaperResult.Error) throw result.exception
                }
        }

    private suspend fun ensureDownloaded(wallpaper: WallpaperModel): File {
        getLocalWallpaperFile(wallpaper.id)?.let { return it }
        var downloaded: File? = null
        downloadWallpaper(wallpaper).collect { state ->
            when (state) {
                is DownloadState.Success -> downloaded = state.file
                is DownloadState.Failure -> throw IOException("Download failed: ${state.error}")
                else -> {}
            }
        }
        return downloaded ?: error("Download produced no file for ${wallpaper.id}")
    }

    private val WallpaperModel.isFree: Boolean
        get() = premium == FREE_WALLPAPER

    private companion object {
        const val FREE_WALLPAPER = 0
    }
}
