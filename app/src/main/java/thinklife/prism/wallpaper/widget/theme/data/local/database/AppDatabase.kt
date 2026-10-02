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

package thinklife.prism.wallpaper.widget.theme.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import thinklife.prism.wallpaper.widget.theme.data.local.ThemeStorageConfig
import thinklife.prism.wallpaper.widget.theme.data.local.WidgetStorageConfig
import thinklife.prism.wallpaper.widget.theme.data.local.dao.DownloadedThemeDao
import thinklife.prism.wallpaper.widget.theme.data.local.dao.DownloadedControllerDao
import thinklife.prism.wallpaper.widget.theme.data.local.dao.DownloadedWallpaperDao
import thinklife.prism.wallpaper.widget.theme.data.local.dao.DownloadedWidgetDao
import thinklife.prism.wallpaper.widget.theme.data.local.entity.DownloadedThemeEntity
import thinklife.prism.wallpaper.widget.theme.data.local.entity.DownloadedControllerEntity
import thinklife.prism.wallpaper.widget.theme.data.local.entity.DownloadedWallpaperEntity
import thinklife.prism.wallpaper.widget.theme.data.local.entity.DownloadedWidgetEntity

@Database(
    entities = [
        DownloadedControllerEntity::class,
        DownloadedWallpaperEntity::class,
        DownloadedThemeEntity::class,
        DownloadedWidgetEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadedThemeDao(): DownloadedControllerDao
    abstract fun downloadedWallpaperDao(): DownloadedWallpaperDao
    abstract fun downloadedP67ThemeDao(): DownloadedThemeDao
    abstract fun downloadedWidgetDao(): DownloadedWidgetDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `${ThemeStorageConfig.DOWNLOADED_THEME_TABLE}` (
                        `themeId` TEXT NOT NULL,
                        `filePath` TEXT NOT NULL,
                        `downloadedAt` INTEGER NOT NULL,
                        `fileSize` INTEGER NOT NULL,
                        PRIMARY KEY(`themeId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `${ThemeStorageConfig.DOWNLOADED_THEME_TABLE}` (
                        `themeId`,
                        `filePath`,
                        `downloadedAt`,
                        `fileSize`
                    )
                    SELECT
                        `themeId`,
                        `filePath`,
                        `downloadedAt`,
                        `fileSize`
                    FROM `downloaded_theme`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE IF EXISTS `downloaded_theme`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `${ThemeStorageConfig.DOWNLOADED_P67_THEME_TABLE}` (
                        `themeId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `thumb` TEXT NOT NULL,
                        `wallpaperThumb` TEXT NOT NULL,
                        `wallpaperUrl` TEXT NOT NULL,
                        `downloadedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`themeId`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `${WidgetStorageConfig.DOWNLOADED_WIDGET_TABLE}` (
                        `widgetId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `thumb` TEXT NOT NULL,
                        `contentS` TEXT NOT NULL,
                        `contentM` TEXT NOT NULL,
                        `contentL` TEXT NOT NULL,
                        `premium` INTEGER NOT NULL,
                        `categoryId` TEXT NOT NULL,
                        `addedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`widgetId`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `${ThemeStorageConfig.DOWNLOADED_THEME_TABLE}` (
                        `themeId` TEXT NOT NULL,
                        `filePath` TEXT NOT NULL,
                        `downloadedAt` INTEGER NOT NULL,
                        `fileSize` INTEGER NOT NULL,
                        PRIMARY KEY(`themeId`)
                    )
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE IF EXISTS `downloaded_theme`")
                db.execSQL(
                    """
                    CREATE TABLE `${ThemeStorageConfig.DOWNLOADED_P67_THEME_TABLE}` (
                        `themeId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `thumb` TEXT NOT NULL,
                        `wallpaperThumb` TEXT NOT NULL,
                        `wallpaperUrl` TEXT NOT NULL,
                        `downloadedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`themeId`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
