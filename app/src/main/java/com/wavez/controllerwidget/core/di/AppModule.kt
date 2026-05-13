package com.wavez.controllerwidget.core.di

import android.content.Context
import com.wavez.controllerwidget.data.repository.SharedPref
import com.wavez.controllerwidget.data.repository.SharedPreferencesImpl
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

    /*@Singleton
    @Provides
    fun provideAppLocalDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context, AppDatabase::class.java, LOCAL_DB_NAME
        ).build()
    }
    @Provides
    fun provideExoPlayerManager(
        @ApplicationContext context: Context
    ): PlayerManager {
        return ExoPlayerManagerImpl(context)
    }

    @Provides
    fun providePreviewService(
        playerManager: PlayerManager,
        sharedPref: SharedPref
    ): PreviewService = PreviewService(playerManager, sharedPref)

    @Provides
    @Singleton
    fun provideLanguageRepository(
        sharedPref: SharedPref,
    ): LanguageRepository = LanguageRepositoryImpl(sharedPref)

    @Provides
    fun provideAppRemoteConfig(
        adsSharedPref: AdsSharedPref,
        sharedPref: SharedPref
    ): AppRemoteConfig {
        return AppRemoteConfigImpl(
            adsSharedPref,
            sharedPref
        )
    }*/


}
