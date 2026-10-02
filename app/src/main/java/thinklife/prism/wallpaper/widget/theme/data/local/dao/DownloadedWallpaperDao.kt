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

package thinklife.prism.wallpaper.widget.theme.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import thinklife.prism.wallpaper.widget.theme.data.local.entity.DownloadedWallpaperEntity
import thinklife.prism.wallpaper.widget.theme.data.local.WallpaperStorageConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedWallpaperDao {

    @Query("SELECT * FROM ${WallpaperStorageConfig.DOWNLOADED_WALLPAPER_TABLE} WHERE wallpaperId = :wallpaperId LIMIT 1")
    suspend fun getByWallpaperId(wallpaperId: String): DownloadedWallpaperEntity?

    @Query("SELECT * FROM ${WallpaperStorageConfig.DOWNLOADED_WALLPAPER_TABLE} ORDER BY downloadedAt DESC")
    fun observeAll(): Flow<List<DownloadedWallpaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadedWallpaperEntity)

    @Query("DELETE FROM ${WallpaperStorageConfig.DOWNLOADED_WALLPAPER_TABLE} WHERE wallpaperId = :wallpaperId")
    suspend fun deleteByWallpaperId(wallpaperId: String)

    @Delete
    suspend fun delete(entity: DownloadedWallpaperEntity)
}
