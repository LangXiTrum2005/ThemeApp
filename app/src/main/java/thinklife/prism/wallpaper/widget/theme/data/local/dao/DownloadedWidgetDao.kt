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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import thinklife.prism.wallpaper.widget.theme.data.local.WidgetStorageConfig
import thinklife.prism.wallpaper.widget.theme.data.local.entity.DownloadedWidgetEntity
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
