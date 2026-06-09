package com.wavez.controllerwidget.di

import com.wavez.controllerwidget.ads.AppRemoteConfig
import com.wavez.controllerwidget.ads.AppRemoteConfigImpl
import com.wavez.controllerwidget.data.repository.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AdsModule {
    @Provides
    fun provideAppRemoteConfig(
        sharedPref: SharedPref,
    ): AppRemoteConfig {
        return AppRemoteConfigImpl(
            sharedPref
        )
    }
}