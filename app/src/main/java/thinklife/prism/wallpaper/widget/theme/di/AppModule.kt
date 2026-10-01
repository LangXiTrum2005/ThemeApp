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

package thinklife.prism.wallpaper.widget.theme.di

import android.content.Context
import androidx.room.Room
import thinklife.prism.wallpaper.widget.theme.core.wallpaper.WallpaperManagerCore
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPref
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPreferencesImpl
import thinklife.prism.wallpaper.widget.theme.data.local.ThemeStorageConfig
import thinklife.prism.wallpaper.widget.theme.data.local.dao.DownloadedControllerDao
import thinklife.prism.wallpaper.widget.theme.data.local.dao.DownloadedThemeDao
import thinklife.prism.wallpaper.widget.theme.data.local.dao.DownloadedWallpaperDao
import thinklife.prism.wallpaper.widget.theme.data.local.dao.DownloadedWidgetDao
import thinklife.prism.wallpaper.widget.theme.data.local.database.AppDatabase
import thinklife.prism.wallpaper.widget.theme.data.repository.IconPackRepository
import thinklife.prism.wallpaper.widget.theme.data.repository.IconPackRepositoryImpl
import thinklife.prism.wallpaper.widget.theme.data.repository.LanguageRepository
import thinklife.prism.wallpaper.widget.theme.data.repository.LanguageRepositoryImpl
import thinklife.prism.wallpaper.widget.theme.data.repository.ThemeControllerRepository
import thinklife.prism.wallpaper.widget.theme.data.repository.ThemeControllerRepositoryImpl
import thinklife.prism.wallpaper.widget.theme.data.repository.ThemeRepository
import thinklife.prism.wallpaper.widget.theme.data.repository.ThemeRepositoryImpl
import thinklife.prism.wallpaper.widget.theme.data.repository.WallpaperRepository
import thinklife.prism.wallpaper.widget.theme.data.repository.WallpaperRepositoryImpl
import thinklife.prism.wallpaper.widget.theme.data.repository.WidgetRepository
import thinklife.prism.wallpaper.widget.theme.data.repository.WidgetRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideSharedPrefProvider(
        @ApplicationContext context: Context,
    ): SharedPref = SharedPreferencesImpl(context)

    @Provides
    @Singleton
    fun provideIconPackRepository(
        impl: IconPackRepositoryImpl,
    ): IconPackRepository = impl

    @Provides
    @Singleton
    fun provideWallpaperRepository(
        impl: WallpaperRepositoryImpl,
    ): WallpaperRepository = impl

    @Provides
    @Singleton
    fun provideWidgetRepository(
        impl: WidgetRepositoryImpl,
    ): WidgetRepository = impl

    @Provides
    @Singleton
    fun provideThemeControllerRepository(
        impl: ThemeControllerRepositoryImpl,
    ): ThemeControllerRepository = impl

    @Provides
    @Singleton
    fun provideThemeRepository(
        impl: ThemeRepositoryImpl,
    ): ThemeRepository = impl

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            ThemeStorageConfig.DATABASE_NAME,
        ).addMigrations(
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
        ).build()
    }

    @Provides
    @Singleton
    fun provideDownloadedThemeDao(
        database: AppDatabase,
    ): DownloadedControllerDao = database.downloadedThemeDao()

    @Provides
    @Singleton
    fun provideDownloadedP67ThemeDao(
        database: AppDatabase,
    ): DownloadedThemeDao = database.downloadedP67ThemeDao()

    @Provides
    @Singleton
    fun provideDownloadedWallpaperDao(
        database: AppDatabase,
    ): DownloadedWallpaperDao = database.downloadedWallpaperDao()

    @Provides
    @Singleton
    fun provideDownloadedWidgetDao(
        database: AppDatabase,
    ): DownloadedWidgetDao = database.downloadedWidgetDao()

    @Provides
    @Singleton
    fun provideLanguageRepository(
        sharedPref: SharedPref,
    ): LanguageRepository = LanguageRepositoryImpl(sharedPref)

    @Provides
    @Singleton
    fun provideWallpaperManagerCore(
        @ApplicationContext context: Context,
    ): WallpaperManagerCore = WallpaperManagerCore(context)
}
