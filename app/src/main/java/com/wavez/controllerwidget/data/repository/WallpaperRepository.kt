package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperTarget
import com.wavez.controllerwidget.data.model.WallpaperCategoryModel
import com.wavez.controllerwidget.data.model.WallpaperModel
import kotlinx.coroutines.flow.Flow
import java.io.File

interface WallpaperRepository {

    suspend fun getCategoriesUnlimited(forceRefresh: Boolean = false): Result<List<WallpaperCategoryModel>>

    suspend fun getAllWallpapersUnlimited(forceRefresh: Boolean = false): Result<List<WallpaperModel>>

    suspend fun getWallpapersByCategory(categoryId: String, forceRefresh: Boolean = false): Result<List<WallpaperModel>>

    fun downloadWallpaper(wallpaper: WallpaperModel): Flow<DownloadState<File>>

    suspend fun checkWallpaperDownloaded(wallpaperId: String): Boolean

    suspend fun getLocalWallpaperFile(wallpaperId: String): File?

    suspend fun applyWallpaper(wallpaper: WallpaperModel, wallpaperTarget: WallpaperTarget): Result<Unit>
}
