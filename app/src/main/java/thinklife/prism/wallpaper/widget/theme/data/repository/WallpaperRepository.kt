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

package thinklife.prism.wallpaper.widget.theme.data.repository

import thinklife.prism.wallpaper.widget.theme.core.download.DownloadState
import thinklife.prism.wallpaper.widget.theme.core.wallpaper.model.WallpaperTarget
import thinklife.prism.wallpaper.widget.theme.data.model.WallpaperCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.model.PopularCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.model.wallpaper.WallpaperModel
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
