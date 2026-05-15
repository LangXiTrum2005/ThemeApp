package com.wavez.controllerwidget.ads

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.wavez.controllerwidget.BuildConfig
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.INTER_ADS_GAP_KEY
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.INTER_OPEN_ADS_GAP_KEY
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.NATIVE_AD_PRELOAD_COUNT_KEY
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.OPEN_ADS_GAP_KEY
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_BANNER_HOME
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_BANNER_INAPP
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_BANNER_SPLASH
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_BOOST_SUCCESS
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_INTER_INAPP
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_INTER_SPLASH
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_FULL_1
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_FULL_2
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE2
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_SETTING_LANGUAGE
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_CONFIG
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_HOME
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_INAPP
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_NATIVE_SELECT_APP
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_OPEN_AD
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_REWARD_CONFIG
import com.wavez.controllerwidget.ads.AppRemoteConfig.Companion.SHOULD_SHOW_REWARD_SELECT
import com.wavez.controllerwidget.data.repository.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppRemoteConfigImpl(
    private val sharedPref: SharedPref,
//    private val adInterstitialManager: AdInterstitialManager,
//    private val openAdManager: OpenAdManager,
//    private val nativeAdRepository: NativeAdRepository,
) : AppRemoteConfig {

    private val configDefaults: Map<String, Any> = mapOf(
        INTER_OPEN_ADS_GAP_KEY to 7_000L,
        OPEN_ADS_GAP_KEY to 7_000L,
        INTER_ADS_GAP_KEY to 30_000L,
        NATIVE_AD_PRELOAD_COUNT_KEY to 3,
        SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE to true,
        SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE2 to true,
        SHOULD_SHOW_NATIVE_AD_ON_SETTING_LANGUAGE to true,
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
        SHOULD_SHOW_BOOST_SUCCESS to true,
        SHOULD_SHOW_NATIVE_CONFIG to true,
        SHOULD_SHOW_REWARD_CONFIG to true,
        SHOULD_SHOW_REWARD_SELECT to true,
        SHOULD_SHOW_NATIVE_SELECT_APP to true
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
                    sharedPref.interOpenAdsGap = remoteConfig.getLong(INTER_OPEN_ADS_GAP_KEY)
                    sharedPref.openAdsGap = remoteConfig.getLong(OPEN_ADS_GAP_KEY)
                    sharedPref.interAdsGap = remoteConfig.getLong(INTER_ADS_GAP_KEY)
                    sharedPref.nativeAdPreloadCount =
                        remoteConfig.getLong(NATIVE_AD_PRELOAD_COUNT_KEY).toInt()
                    Log.d(
                        "log_remote",
                        "fetchRemoteConfig: ${remoteConfig.getBoolean(SHOULD_SHOW_BANNER_SPLASH)}"
                    )
                    sharedPref.shouldShowNativeLanguage1 =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE)
                    sharedPref.shouldShowNativeLanguage2 =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE2)
                    sharedPref.shouldShowNativeAdFull1 =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_FULL_1)
                    sharedPref.shouldShowNativeAdFull2 =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_FULL_2)
                    sharedPref.shouldShowInterSplash =
                        remoteConfig.getBoolean(SHOULD_SHOW_INTER_SPLASH)
                    sharedPref.shouldBannerSplash =
                        remoteConfig.getBoolean(SHOULD_SHOW_BANNER_SPLASH)
                    sharedPref.shouldShowNativeObd1 =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1)
                    sharedPref.shouldShowNativeObd1Click =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK)
                    sharedPref.shouldShowBannerHome =
                        remoteConfig.getBoolean(SHOULD_SHOW_BANNER_HOME)
                    sharedPref.shouldShowNativeHome =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_HOME)
                    sharedPref.shouldShowBannerInApp =
                        remoteConfig.getBoolean(SHOULD_SHOW_BANNER_INAPP)
                    sharedPref.shouldShowNativeInApp =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_INAPP)
                    sharedPref.shouldShowInterInApp =
                        remoteConfig.getBoolean(SHOULD_SHOW_INTER_INAPP)
                    sharedPref.shouldShowOpenAd = remoteConfig.getBoolean(SHOULD_SHOW_OPEN_AD)
                    sharedPref.shouldShowNativeBoostSuccess =
                        remoteConfig.getBoolean(SHOULD_SHOW_BOOST_SUCCESS)
                    sharedPref.shouldShowNativeSelectApp =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_SELECT_APP)
                    sharedPref.shouldShowNativeConfig =
                        remoteConfig.getBoolean(SHOULD_SHOW_NATIVE_CONFIG)
                    sharedPref.shouldShowRewardConfig =
                        remoteConfig.getBoolean(SHOULD_SHOW_REWARD_CONFIG)
                    sharedPref.shouldShowRewardSelect =
                        remoteConfig.getBoolean(SHOULD_SHOW_REWARD_SELECT)
                    setupAds()
                }
            }
        }
    }

    private fun setupAds() {
        setupOpenAdManager()
        setupAdInterstitialManager()
    }

    private fun setupOpenAdManager() {
//        openAdManager.setShowCondition(object : ShowCondition {
//            override fun shouldShow(): Boolean {
//                val isOverGap =
//                    System.currentTimeMillis() - openAdManager.dismissFullScreenLastTime >= sharedPref.openAdsGap
//                val isOverGapWithInterAd =
//                    System.currentTimeMillis() - adInterstitialManager.dismissFullScreenLastTime >= sharedPref.interOpenAdsGap
//                return isOverGap && isOverGapWithInterAd && sharedPref.shouldShowOpenAd
//            }
//        })
    }

    private fun setupAdInterstitialManager() {
//        adInterstitialManager.setShowCondition(object : ShowCondition {
//            override fun shouldShow(): Boolean {
//                val isOverGap =
//                    System.currentTimeMillis() - adInterstitialManager.dismissFullScreenLastTime >= sharedPref.interAdsGap
//                Log.d(
//                    "ádadadad",
//                    "shouldShow: $isOverGap --  ${sharedPref.interAdsGap}  ${System.currentTimeMillis() - adInterstitialManager.dismissFullScreenLastTime}"
//                )
//
//                val isOverGapWithInterAd =
//                    System.currentTimeMillis() - openAdManager.dismissFullScreenLastTime >= sharedPref.interOpenAdsGap
//                return isOverGap && isOverGapWithInterAd && sharedPref.shouldShowInterInApp
//            }
//        })
    }

}