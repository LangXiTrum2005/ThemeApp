package com.wavez.themewallpaper.di

import com.wavez.themewallpaper.core.config.AppRemoteConfig
import com.wavez.themewallpaper.core.config.AppRemoteConfigImpl
import com.wavez.themewallpaper.data.repository.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RemoteConfigModule {
    @Provides
    fun provideAppRemoteConfig(
        sharedPref: SharedPref,
    ): AppRemoteConfig {
        return AppRemoteConfigImpl(
            sharedPref
        )
    }
}
