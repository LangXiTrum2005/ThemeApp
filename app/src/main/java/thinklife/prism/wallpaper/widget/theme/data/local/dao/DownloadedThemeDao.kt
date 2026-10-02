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
import thinklife.prism.wallpaper.widget.theme.data.local.ThemeStorageConfig
import thinklife.prism.wallpaper.widget.theme.data.local.entity.DownloadedThemeEntity
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
