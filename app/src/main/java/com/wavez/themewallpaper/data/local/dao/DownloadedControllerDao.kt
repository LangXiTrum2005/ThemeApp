package com.wavez.themewallpaper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wavez.themewallpaper.data.local.entity.DownloadedControllerEntity
import com.wavez.themewallpaper.data.local.ThemeStorageConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedControllerDao {

    @Query("SELECT * FROM ${ThemeStorageConfig.DOWNLOADED_THEME_TABLE} WHERE themeId = :themeId LIMIT 1")
    suspend fun getByThemeId(themeId: String): DownloadedControllerEntity?

    @Query("SELECT * FROM ${ThemeStorageConfig.DOWNLOADED_THEME_TABLE} ORDER BY downloadedAt DESC")
    fun observeAll(): Flow<List<DownloadedControllerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadedControllerEntity)

    @Query("DELETE FROM ${ThemeStorageConfig.DOWNLOADED_THEME_TABLE} WHERE themeId = :themeId")
    suspend fun deleteByThemeId(themeId: String)

    @Delete
    suspend fun delete(entity: DownloadedControllerEntity)
}
