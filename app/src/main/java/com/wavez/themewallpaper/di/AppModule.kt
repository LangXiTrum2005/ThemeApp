package com.wavez.themewallpaper.di

import android.content.Context
import androidx.room.Room
import com.src.billing_library.BillingLibrary
import com.src.billing_library.GooglePlayBillingLibrary
import com.wavez.themewallpaper.core.wallpaper.WallpaperManagerCore
import com.wavez.themewallpaper.data.local.ThemeStorageConfig
import com.wavez.themewallpaper.data.local.dao.DownloadedControllerDao
import com.wavez.themewallpaper.data.local.dao.DownloadedThemeDao
import com.wavez.themewallpaper.data.local.dao.DownloadedWallpaperDao
import com.wavez.themewallpaper.data.local.database.AppDatabase
import com.wavez.themewallpaper.data.repository.IconPackRepository
import com.wavez.themewallpaper.data.repository.IconPackRepositoryImpl
import com.wavez.themewallpaper.data.repository.LanguageRepository
import com.wavez.themewallpaper.data.repository.LanguageRepositoryImpl
import com.wavez.themewallpaper.data.repository.PremiumRepository
import com.wavez.themewallpaper.data.repository.SharedPref
import com.wavez.themewallpaper.data.repository.SharedPreferencesImpl
import com.wavez.themewallpaper.data.repository.ThemeControllerRepository
import com.wavez.themewallpaper.data.repository.ThemeControllerRepositoryImpl
import com.wavez.themewallpaper.data.repository.ThemeRepository
import com.wavez.themewallpaper.data.repository.ThemeRepositoryImpl
import com.wavez.themewallpaper.data.repository.WallpaperRepository
import com.wavez.themewallpaper.data.repository.WallpaperRepositoryImpl
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
            ThemeStorageConfig.DATABASE_NAME
        ).addMigrations(
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
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
    fun provideLanguageRepository(
        sharedPref: SharedPref,
    ): LanguageRepository = LanguageRepositoryImpl(sharedPref)

    @Provides
    @Singleton
    fun provideWallpaperManagerCore(
        @ApplicationContext context: Context,
    ): WallpaperManagerCore = WallpaperManagerCore(context)


    @Provides
    @Singleton
    fun providerBillingManager(
        @ApplicationContext context: Context,
    ): BillingLibrary {
        return GooglePlayBillingLibrary(context)
    }


    @Provides
    @Singleton
    fun providerPremiumRepository(
        billingLibrary: BillingLibrary,
        sharedPref: SharedPref
    ): PremiumRepository {
        return PremiumRepository(billingLibrary, sharedPref)
    }

}
