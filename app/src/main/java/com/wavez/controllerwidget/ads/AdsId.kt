package com.wavez.controllerwidget.ads

import com.wavez.controllerwidget.BuildConfig


/**
 * Centralized Ad unit ids. Grouped by category for readability.
 * Public API is kept unchanged to avoid breaking callers.
 */
object AdsId {

    // --- Test constants ---
    private const val BANNER_TEST_ADS = "ca-app-pub-3940256099942544/6300978111"
    private const val REWARD_TEST_ADS = "ca-app-pub-3940256099942544/5224354917"
    private const val POPUP_TEST_ADS = "ca-app-pub-3940256099942544/1033173712"
    private const val OPEN_UNIT_ID_TEST = "ca-app-pub-3940256099942544/9257395921"
    private const val NATIVE_AD_ID_TEST = "ca-app-pub-3940256099942544/2247696110"

    // --- Banner ---
    fun getBannerHomeAds(): String {
        return if (BuildConfig.DEBUG) {
            BANNER_TEST_ADS
        } else {
            "ca-app-pub-3438626400465865/8934867060"
        }
    }

    fun getBannerAdsSplash(): String {
        return if (BuildConfig.DEBUG) {
            BANNER_TEST_ADS
        } else {
            "ca-app-pub-3438626400465865/5316470119"
        }
    }

    // --- Interstitial / Popup lists ---
    fun getListInterstitialAdsSplash(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(POPUP_TEST_ADS)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/9670648091",
                "ca-app-pub-3438626400465865/3936095363"
            )
        }
    }

    fun getListInterstitialInAppAds(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(POPUP_TEST_ADS)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/5487111106",
                "ca-app-pub-5390451356176712/8630382835"
            )
        }
    }

    // --- Native: language / onboarding / obd / home / config / select / in-app ---
    fun getListAdNativeLanguageAds1(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/5033554610",
                "ca-app-pub-3438626400465865/8854976017"
            )
        }
    }

    fun getListAdNativeLanguageAds2(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/6556115739",
                "ca-app-pub-3438626400465865/7541894347"
            )
        }
    }

    fun getListAdNativeOnboarding1(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/8990707380",
                "ca-app-pub-3438626400465865/8984252384"
            )
        }
    }

    fun getListAdNativeOnboarding1Click(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/1237624305",
                "ca-app-pub-3438626400465865/9307686014"
            )
        }
    }

    fun getListAdNativeObdFull1(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/7511388619",
                "ca-app-pub-3438626400465865/6298379292"
            )
        }
    }

    fun getListAdNativeObdFull2(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/7339998221",
                "ca-app-pub-3438626400465865/4793725933"
            )
        }
    }

    fun getNativeHomeAds(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/8994576815",
                "ca-app-pub-3438626400465865/1307658482",
            )
        }
    }

    fun getNativeBoostSuccessAds(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/6145270399",
                "ca-app-pub-3438626400465865/1514926093",
            )
        }
    }

    fun getNativeConfig1(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/6786150210",
            )
        }
    }

    fun getNativeConfig2(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/2659202947",
            )
        }
    }

    fun getNativeConfig3(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/1697475067",
            )
        }
    }

    fun getNativeConfig4(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/7719957932",
            )
        }
    }

    fun getNativeSelectApp(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/6368413471",
            )
        }
    }


    // --- Rewarded ---
    fun getRewardedSelectAds(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(REWARD_TEST_ADS)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/3360847469",
                "ca-app-pub-3438626400465865/2239337487"
            )
        }
    }

    fun getRewardedAdsConfig(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(REWARD_TEST_ADS)
        } else {
            listOf(
                "ca-app-pub-3438626400465865/5696429821",
                "ca-app-pub-3438626400465865/9220741865"
            )
        }
    }

    // --- Open ---
    fun getOpenAds(): String {
        return if (BuildConfig.DEBUG) {
            OPEN_UNIT_ID_TEST
        } else {
            "ca-app-pub-3438626400465865/2259061939"
        }
    }
}