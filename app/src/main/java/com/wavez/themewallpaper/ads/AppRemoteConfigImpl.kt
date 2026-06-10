package com.wavez.themewallpaper.ads

import android.util.Log
import com.ads.admob.MMonetize
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.wavez.themewallpaper.BuildConfig
import com.wavez.themewallpaper.Secrets
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.INTER_ADS_GAP_KEY
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.INTER_OPEN_ADS_GAP_KEY
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.NATIVE_AD_PRELOAD_COUNT_KEY
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.NATIVE_GAP
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.OPEN_ADS_GAP_KEY
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SERVER_ROOT
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_BANNER_HOME
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_BANNER_INAPP
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_BANNER_SPLASH
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_INTER_INAPP
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_INTER_SPLASH
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_FULL_1
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_FULL_2
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE2
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_SETTING_LANGUAGE
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_2
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_3
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_HOME
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_INAPP
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_SELECT_APP
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_OPEN_AD
import com.wavez.themewallpaper.ads.AppRemoteConfig.Companion.SHOULD_SHOW_REWARD
import com.wavez.themewallpaper.data.repository.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppRemoteConfigImpl(
    private val sharedPref: SharedPref,
) : AppRemoteConfig {

    private val configDefaults: Map<String, Any> = mapOf(
        INTER_OPEN_ADS_GAP_KEY to 3_000L,
        OPEN_ADS_GAP_KEY to 3_000L,
        INTER_ADS_GAP_KEY to 30_000L,
        NATIVE_GAP to 15_000L,
        SERVER_ROOT to Secrets.INSTANCE.getBaseURL(),
        NATIVE_AD_PRELOAD_COUNT_KEY to 3,
        SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE to true,
        SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE2 to true,
        SHOULD_SHOW_NATIVE_AD_ON_SETTING_LANGUAGE to true,
        SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1 to true,
        SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK to true,
        SHOULD_SHOW_NATIVE_AD_FULL_1 to true,
        SHOULD_SHOW_NATIVE_AD_FULL_2 to true,
        SHOULD_SHOW_BANNER_SPLASH to true,
        SHOULD_SHOW_INTER_SPLASH to true,
        SHOULD_SHOW_BANNER_HOME to true,
        SHOULD_SHOW_BANNER_INAPP to true,
        SHOULD_SHOW_NATIVE_INAPP to true,
        SHOULD_SHOW_INTER_INAPP to true,
        SHOULD_SHOW_NATIVE_HOME to true,
        SHOULD_SHOW_OPEN_AD to true,
        SHOULD_SHOW_REWARD to true,
        SHOULD_SHOW_NATIVE_SELECT_APP to true,
        SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_2 to true,
        SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_3 to true,
    )

    override fun fetchRemoteConfig() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setDefaultsAsync(configDefaults)
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                CoroutineScope(Dispatchers.IO).launch {
                    val canShowAds = !sharedPref.isVip
                    fun remoteAdEnabled(key: String): Boolean {
                        return canShowAds && remoteConfig.getBoolean(key)
                    }
                    sharedPref.severRoot = remoteConfig.getString(SERVER_ROOT)
                    sharedPref.interOpenAdsGap = remoteConfig.getLong(INTER_OPEN_ADS_GAP_KEY)
                    sharedPref.openAdsGap = remoteConfig.getLong(OPEN_ADS_GAP_KEY)
                    sharedPref.interAdsGap = remoteConfig.getLong(INTER_ADS_GAP_KEY)
                    sharedPref.nativeAdPreloadCount =
                        remoteConfig.getLong(NATIVE_AD_PRELOAD_COUNT_KEY).toInt()
                    sharedPref.nativeGap = remoteConfig.getLong(NATIVE_GAP)
                    Log.d(
                        "log_remote",
                        "fetchRemoteConfig: ${remoteConfig.getBoolean(SHOULD_SHOW_BANNER_INAPP)}"
                    )
                    sharedPref.shouldShowNativeLanguage1 =
                        remoteAdEnabled(SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE)
                    sharedPref.shouldShowNativeLanguage2 =
                        remoteAdEnabled(SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE2)
                    sharedPref.shouldShowNativeAdFull1 =
                        remoteAdEnabled(SHOULD_SHOW_NATIVE_AD_FULL_1)
                    sharedPref.shouldShowNativeAdFull2 =
                        remoteAdEnabled(SHOULD_SHOW_NATIVE_AD_FULL_2)
                    sharedPref.shouldShowInterSplash = remoteAdEnabled(SHOULD_SHOW_INTER_SPLASH)
                    sharedPref.shouldBannerSplash = remoteAdEnabled(SHOULD_SHOW_BANNER_SPLASH)
                    sharedPref.shouldShowNativeObd1 =
                        remoteAdEnabled(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1)
                    sharedPref.shouldShowNativeObd1Click =
                        remoteAdEnabled(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK)
                    sharedPref.shouldShowNativeObd2 =
                        remoteAdEnabled(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_2)
                    sharedPref.shouldShowNativeObd3 =
                        remoteAdEnabled(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_3)
                    sharedPref.shouldShowBannerHome = remoteAdEnabled(SHOULD_SHOW_BANNER_HOME)
                    sharedPref.shouldShowNativeHome = remoteAdEnabled(SHOULD_SHOW_NATIVE_HOME)
                    sharedPref.shouldShowBannerInApp = remoteAdEnabled(SHOULD_SHOW_BANNER_INAPP)
                    sharedPref.shouldShowNativeInApp = remoteAdEnabled(SHOULD_SHOW_NATIVE_INAPP)
                    sharedPref.shouldShowInterInApp = remoteAdEnabled(SHOULD_SHOW_INTER_INAPP)
                    sharedPref.shouldShowOpenAd = remoteAdEnabled(SHOULD_SHOW_OPEN_AD)
                    sharedPref.shouldShowReward = remoteAdEnabled(SHOULD_SHOW_REWARD)
                    sharedPref.shouldShowNativeSelectApp =
                        remoteAdEnabled(SHOULD_SHOW_NATIVE_SELECT_APP)
                    setupAds()
                }
            }
        }
    }

    private fun setupAds() {
        setupOpenAdManager()
        setupAdInterstitialManager()
        setUpNativeAdRepository()
    }

    private fun setUpNativeAdRepository() {
        MMonetize.nativeAdRepository.setLoadCondition {
            val isOverGap = System.currentTimeMillis() -
                    MMonetize.nativeAdRepository.getLastLoadTime() >= sharedPref.nativeGap
            isOverGap
        }
    }

    private fun setupOpenAdManager() {
        MMonetize.openAdManager.setAdUnitId(AdsId.getOpenAds())
        MMonetize.openAdManager.setShowCondition {
            val isOverGap = System.currentTimeMillis() -
                    MMonetize.openAdManager.dismissFullScreenLastTime >= sharedPref.openAdsGap
            val isOverGapWithInter = System.currentTimeMillis() -
                    MMonetize.interstitialManager.dismissFullScreenLastTime >= sharedPref.interOpenAdsGap
            isOverGap && isOverGapWithInter && sharedPref.shouldShowOpenAd
        }
    }

    private fun setupAdInterstitialManager() {
        MMonetize.interstitialManager.setAdUnitIds(AdsId.getListInterstitialInAppAds())
        MMonetize.interstitialManager.setShowCondition {
            val isOverGap = System.currentTimeMillis() -
                    MMonetize.interstitialManager.dismissFullScreenLastTime >= sharedPref.interAdsGap
            val isOverGapWithOpen = System.currentTimeMillis() -
                    MMonetize.openAdManager.dismissFullScreenLastTime >= sharedPref.interOpenAdsGap
            isOverGap && isOverGapWithOpen && sharedPref.shouldShowInterInApp
        }
    }

}
