package com.wavez.themewallpaper.data.repository

import com.wavez.themewallpaper.core.download.DownloadState
import com.wavez.themewallpaper.core.wallpaper.model.WallpaperTarget
import com.wavez.themewallpaper.data.model.WallpaperCategoryModel
import com.wavez.themewallpaper.data.model.PopularCategoryModel
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel
import kotlinx.coroutines.flow.Flow
import java.io.File

interface WallpaperRepository {

    suspend fun getCategoriesUnlimited(forceRefresh: Boolean = false): Result<List<WallpaperCategoryModel>>

    suspend fun getAllWallpapersUnlimited(forceRefresh: Boolean = false): Result<List<WallpaperModel>>

    suspend fun getMostPopularWallpapers(forceRefresh: Boolean = false): Result<List<PopularCategoryModel>>

    suspend fun getWallpapersByCategory(categoryId: String, forceRefresh: Boolean = false): Result<List<WallpaperModel>>

    fun downloadWallpaper(wallpaper: WallpaperModel): Flow<DownloadState<File>>

    suspend fun checkWallpaperDownloaded(wallpaperId: String): Boolean

    suspend fun getLocalWallpaperFile(wallpaperId: String): File?

    fun observeDownloadedWallpapers(): Flow<List<WallpaperModel>>

    suspend fun applyWallpaper(wallpaper: WallpaperModel, wallpaperTarget: WallpaperTarget): Result<Unit>
}
