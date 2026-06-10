package com.wavez.themewallpaper.data.repository

import android.content.Context
import android.content.Intent
import com.wavez.themewallpaper.core.controllercenter.ThemeLoader
import com.wavez.themewallpaper.core.controllercenter.model.ControllerConfigModel
import com.wavez.themewallpaper.core.controllercenter.service.ControlCenterService
import com.wavez.themewallpaper.di.IoDispatcher
import com.wavez.themewallpaper.core.download.DownloadState
import com.wavez.themewallpaper.core.download.ThemeDownloader
import com.wavez.themewallpaper.data.local.dao.DownloadedControllerDao
import com.wavez.themewallpaper.data.local.entity.DownloadedControllerEntity
import com.wavez.themewallpaper.data.local.controller.ControllerStorageManager
import com.wavez.themewallpaper.data.model.ControllerCategoryModel
import com.wavez.themewallpaper.data.model.ControllerModel
import com.wavez.themewallpaper.data.model.DownloadedResInfo
import com.wavez.themewallpaper.data.network.api.ControllerWidgetApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ThemeControllerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ControllerWidgetApiService,
    private val downloadedThemeDao: DownloadedControllerDao,
    private val themeStorageManager: ControllerStorageManager,
    private val themeDownloader: ThemeDownloader,
    private val sharedPref: SharedPref,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ThemeControllerRepository {

    private var cachedCategories: List<ControllerCategoryModel>? = null
    private var cachedControllerThemes: List<ControllerModel>? = null

    override suspend fun getCategoriesController(
        forceRefresh: Boolean
    ): Result<List<ControllerCategoryModel>> {
        if (!forceRefresh) {
            cachedCategories?.let { return Result.success(it) }
        }

        return repositoryRequest { apiService.getCategoriesController() }
            .onSuccess { cachedCategories = it }
    }

    override suspend fun getAllControllerTheme(
        forceRefresh: Boolean
    ): Result<List<ControllerModel>> {
        if (!forceRefresh) {
            cachedControllerThemes?.let { return Result.success(it) }
        }

        return repositoryRequest { apiService.getAllControllerTheme() }
            .onSuccess { cachedControllerThemes = it }
    }

    override suspend fun checkDownloaded(themeId: String): Boolean = withContext(ioDispatcher) {
        val entity = downloadedThemeDao.getByThemeId(themeId) ?: return@withContext false
        val file = File(entity.filePath)
        if (file.isFile) {
            true
        } else {
            downloadedThemeDao.deleteByThemeId(themeId)
            false
        }
    }

    override suspend fun getLocalThemeFile(themeId: String): File? = withContext(ioDispatcher) {
        val entity = downloadedThemeDao.getByThemeId(themeId) ?: return@withContext null
        val file = File(entity.filePath)
        if (file.isFile) {
            file
        } else {
            downloadedThemeDao.deleteByThemeId(themeId)
            null
        }
    }

    override fun observeDownloadedThemes(): Flow<List<ControllerModel>> =
        downloadedThemeDao.observeAll()
            .map { entities ->
                val downloadedIds = entities.map { it.themeId }.toSet()
                val order = entities.mapIndexed { index, entity -> entity.themeId to index }
                    .toMap()
                getAllControllerTheme()
                    .getOrDefault(emptyList())
                    .filter { it.id in downloadedIds }
                    .sortedBy { order[it.id] ?: Int.MAX_VALUE }
            }
            .flowOn(ioDispatcher)

    override suspend fun applyTheme(theme: ControllerModel): Result<ControllerConfigModel> {
        return withContext(ioDispatcher) {
            runCatching {
                getLocalThemeFile(theme.id)
                    ?: error("Theme is not downloaded: ${theme.id}")
                val themeDir = themeStorageManager.prepareThemeForApply(theme.id)
                val config = ThemeLoader.loadFromDirectory(themeDir, theme.id)
                // Persist so service and future sessions load the correct theme
                sharedPref.activeThemeId = theme.id
                // Notify ControlCenterService to reload its overlay with the new theme
                context.sendBroadcast(
                    Intent(ControlCenterService.ACTION_RELOAD_THEME)
                        .setPackage(context.packageName)
                )
                config
            }
        }
    }

    override fun downloadControllerTheme(theme: ControllerModel): Flow<DownloadState<File>> = flow {
        // Short-circuit: already fully downloaded
        if (checkDownloaded(theme.id)) {
            val zipFile = themeStorageManager.getThemeFile(theme.id)
            emit(DownloadState.Success(zipFile))
            return@flow
        }
        // Remove any orphaned DB record before starting fresh
        downloadedThemeDao.deleteByThemeId(theme.id)

        themeDownloader.download(theme.id, theme.url).collect { state ->
            when (state) {
                is DownloadState.Saving -> {
                    emit(DownloadState.Saving)
                    // File is fully on disk — persist Room record
                    val zipFile = themeStorageManager.getThemeFile(theme.id)
                    downloadedThemeDao.upsert(
                        DownloadedControllerEntity(
                            themeId = theme.id,
                            filePath = zipFile.absolutePath,
                            downloadedAt = System.currentTimeMillis(),
                            fileSize = zipFile.length(),
                        )
                    )
                    // Fire-and-forget stats update; failure must not block the download result
                    runCatching {
                        DownloadedResInfo.fromResource(theme)
                            ?.let { apiService.updateWallpaperDownloadAmount(it) }
                    }
                    emit(DownloadState.Success(zipFile))
                }
                else -> emit(state)
            }
        }
    }.flowOn(ioDispatcher)
}
