package com.ads.admob

import com.ads.admob.interstitial_ads.AdInterstitialManager
import com.ads.admob.interstitial_ads.AdInterstitialManagerImpl
import com.ads.admob.native_ads.NativeAdRepository
import com.ads.admob.native_ads.NativeAdRepositoryImpl
import com.ads.admob.open_ads.OpenAdManager
import com.ads.admob.open_ads.OpenAdManagerImpl
import com.ads.admob.reward.RewardAdManager
import com.ads.admob.reward.RewardAdManagerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for the ads library.
 * Creates singletons for all ad managers.
 *
 * Ad unit IDs and show/load conditions are configured separately
 * by the app via [AppRemoteConfig] after Remote Config is fetched.
 */
internal fun kenAdsModule() = module {

    single<NativeAdRepository> {
        NativeAdRepositoryImpl(androidContext())
    }

    single<AdInterstitialManager> {
        AdInterstitialManagerImpl(androidContext())
    }

    single<OpenAdManager> {
        OpenAdManagerImpl(androidContext())
    }

    single<RewardAdManager> {
        RewardAdManagerImpl(androidContext())
    }
}
