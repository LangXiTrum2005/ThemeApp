package com.wavez.themewallpaper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wavez.themewallpaper.data.local.WidgetStorageConfig
import com.wavez.themewallpaper.data.local.entity.DownloadedWidgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedWidgetDao {

    @Query("SELECT * FROM ${WidgetStorageConfig.DOWNLOADED_WIDGET_TABLE} ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<DownloadedWidgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadedWidgetEntity)

    @Query("DELETE FROM ${WidgetStorageConfig.DOWNLOADED_WIDGET_TABLE} WHERE widgetId = :widgetId")
    suspend fun deleteByWidgetId(widgetId: String)
}
