package com.wavez.controllerwidget.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wavez.controllerwidget.data.local.entity.DownloadedWallpaperEntity
import com.wavez.controllerwidget.data.local.WallpaperStorageConfig
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
