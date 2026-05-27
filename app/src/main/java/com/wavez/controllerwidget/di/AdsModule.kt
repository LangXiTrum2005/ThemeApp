package com.wavez.controllerwidget.di

import android.content.Context
import com.ads.admob.LoadCondition
import com.ads.admob.ShowCondition
import com.ads.admob.interstitial_ads.AdInterstitialManager
import com.ads.admob.interstitial_ads.AdInterstitialManagerImpl
import com.ads.admob.native_ads.NativeAdRepository
import com.ads.admob.native_ads.NativeAdRepositoryImpl
import com.ads.admob.open_ads.OpenAdManager
import com.ads.admob.open_ads.OpenAdManagerImpl
import com.ads.admob.reward.RewardAdManager
import com.ads.admob.reward.RewardAdManagerImpl
import com.wavez.controllerwidget.ads.AdsId
import com.wavez.controllerwidget.ads.AppRemoteConfig
import com.wavez.controllerwidget.ads.AppRemoteConfigImpl
import com.wavez.controllerwidget.data.repository.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AdsModule {

    @Provides
    @Singleton
    fun provideShouldShowAdCondition(
        sharedPref: SharedPref
    ): ShowCondition {
        return object : ShowCondition {
            override fun shouldShow(): Boolean {
                val shouldShow = !sharedPref.isVip
                return shouldShow
            }
        }
    }

    @Provides
    @Singleton
    fun provideShouldLoadAdCondition(
        sharedPref: SharedPref
    ): LoadCondition {
        return object : LoadCondition {
            override fun shouldLoad(): Boolean {
                val shouldLoad = !sharedPref.isVip
                return shouldLoad
            }
        }
    }

    @Provides
    @Singleton
    fun provideNativeAdRepository(
        @ApplicationContext context: Context,
        showCondition: ShowCondition,
        loadCondition: LoadCondition
    ): NativeAdRepository {
        return NativeAdRepositoryImpl(context).apply {
            setShowCondition(showCondition)
            setLoadCondition(loadCondition)
        }
    }

    @Provides
    @Singleton
    fun provideAdInterstitialManager(
        @ApplicationContext context: Context,
        showCondition: ShowCondition,
        loadCondition: LoadCondition
    ): AdInterstitialManager {
        return AdInterstitialManagerImpl(context).apply {
            setAdUnitIds(AdsId.getListInterstitialInAppAds())
            setShowCondition(showCondition)
            setLoadCondition(loadCondition)
        }
    }

    @Provides
    @Singleton
    fun provideOpenAdManager(
        @ApplicationContext context: Context,
        showCondition: ShowCondition,
        loadCondition: LoadCondition
    ): OpenAdManager {
        return OpenAdManagerImpl(context).apply {
            setAdUnitId(AdsId.getOpenAds())
            setShowCondition(showCondition)
            setLoadCondition(loadCondition)
        }
    }

    @Provides
    @Singleton
    fun provideRewardAdManager(
        @ApplicationContext context: Context
    ): RewardAdManager {
        return RewardAdManagerImpl(context)
    }

    @Provides
    fun provideAppRemoteConfig(
        sharedPref: SharedPref,
        interstitialManager: AdInterstitialManager,
        openAdManager: OpenAdManager,
    ): AppRemoteConfig {
        return AppRemoteConfigImpl(
            sharedPref, interstitialManager, openAdManager
        )
    }
}