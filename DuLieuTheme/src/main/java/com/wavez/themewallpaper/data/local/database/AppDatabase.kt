package com.wavez.themewallpaper.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wavez.themewallpaper.data.local.ThemeStorageConfig
import com.wavez.themewallpaper.data.local.WidgetStorageConfig
import com.wavez.themewallpaper.data.local.dao.DownloadedThemeDao
import com.wavez.themewallpaper.data.local.dao.DownloadedControllerDao
import com.wavez.themewallpaper.data.local.dao.DownloadedWallpaperDao
import com.wavez.themewallpaper.data.local.dao.DownloadedWidgetDao
import com.wavez.themewallpaper.data.local.entity.DownloadedThemeEntity
import com.wavez.themewallpaper.data.local.entity.DownloadedControllerEntity
import com.wavez.themewallpaper.data.local.entity.DownloadedWallpaperEntity
import com.wavez.themewallpaper.data.local.entity.DownloadedWidgetEntity

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
