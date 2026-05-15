package com.wavez.controllerwidget.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wavez.controllerwidget.data.local.dao.DownloadedThemeDao
import com.wavez.controllerwidget.data.local.dao.DownloadedWallpaperDao
import com.wavez.controllerwidget.data.local.entity.DownloadedThemeEntity
import com.wavez.controllerwidget.data.local.entity.DownloadedWallpaperEntity

@Database(
    entities = [DownloadedThemeEntity::class, DownloadedWallpaperEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadedThemeDao(): DownloadedThemeDao
    abstract fun downloadedWallpaperDao(): DownloadedWallpaperDao
}
