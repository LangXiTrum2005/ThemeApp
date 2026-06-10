package com.wavez.themewallpaper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wavez.themewallpaper.data.local.ThemeStorageConfig
import com.wavez.themewallpaper.data.local.entity.DownloadedThemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedThemeDao {

    @Query("SELECT * FROM ${ThemeStorageConfig.DOWNLOADED_P67_THEME_TABLE} WHERE themeId = :themeId LIMIT 1")
    suspend fun getByThemeId(themeId: String): DownloadedThemeEntity?

    @Query("SELECT * FROM ${ThemeStorageConfig.DOWNLOADED_P67_THEME_TABLE} ORDER BY themeId DESC")
    fun getThemeAll(): Flow<List<DownloadedThemeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadedThemeEntity)

    @Query("DELETE FROM ${ThemeStorageConfig.DOWNLOADED_P67_THEME_TABLE} WHERE themeId = :themeId")
    suspend fun deleteByThemeId(themeId: String)

    @Delete
    suspend fun delete(entity: DownloadedThemeEntity)
}
