package com.wavez.themewallpaper.data.repository

import android.net.Uri
import android.util.Log
import com.wavez.themewallpaper.core.download.DownloadState
import com.wavez.themewallpaper.core.download.WallpaperDownloader
import com.wavez.themewallpaper.core.wallpaper.WallpaperManagerCore
import com.wavez.themewallpaper.core.wallpaper.model.WallpaperOptions
import com.wavez.themewallpaper.core.wallpaper.model.WallpaperResult
import com.wavez.themewallpaper.core.wallpaper.model.WallpaperSource
import com.wavez.themewallpaper.core.wallpaper.model.WallpaperTarget
import com.wavez.themewallpaper.data.local.dao.DownloadedWallpaperDao
import com.wavez.themewallpaper.data.local.entity.DownloadedWallpaperEntity
import com.wavez.themewallpaper.data.local.wallpaper.WallpaperStorageManager
import com.wavez.themewallpaper.data.model.DownloadedResInfo
import com.wavez.themewallpaper.data.model.PopularCategoryModel
import com.wavez.themewallpaper.data.model.WallpaperCategoryModel
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperType
import com.wavez.themewallpaper.data.model.wallpaper.wallpaperType
import com.wavez.themewallpaper.data.network.api.ApiService
import com.wavez.themewallpaper.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

class WallpaperRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val wallpaperDownloader: WallpaperDownloader,
    private val wallpaperStorageManager: WallpaperStorageManager,
    private val downloadedWallpaperDao: DownloadedWallpaperDao,
    private val wallpaperManagerCore: WallpaperManagerCore,
    private val sharedPref: SharedPref,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : WallpaperRepository {

    private var cachedCategories: List<WallpaperCategoryModel>? = null
    private var cachedWallpapers: List<WallpaperModel>? = null
    private var cachedMostPopularWallpapers: List<PopularCategoryModel>? = null

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

    override suspend fun getMostPopularWallpapers(
        forceRefresh: Boolean
    ): Result<List<PopularCategoryModel>> {
        if (!forceRefresh) {
            cachedMostPopularWallpapers?.let { return Result.success(it) }
        }

        return repositoryRequest { apiService.getMostPopularWallpapers() }
            .onSuccess { cachedMostPopularWallpapers = it }
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
                        }.onFailure {
                            Log.e("WallpaperRepository", "updateWallpaperDownloadAmount failed", it)
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

    override fun observeDownloadedWallpapers(): Flow<List<WallpaperModel>> =
        downloadedWallpaperDao.observeAll()
            .map { entities ->
                val downloadedIds = entities.map { it.wallpaperId }.toSet()
                val order = entities.mapIndexed { index, entity -> entity.wallpaperId to index }
                    .toMap()
                getAllWallpapersUnlimited()
                    .getOrDefault(emptyList())
                    .filter { it.id in downloadedIds }
                    .sortedBy { order[it.id] ?: Int.MAX_VALUE }
            }
            .flowOn(ioDispatcher)

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

}
