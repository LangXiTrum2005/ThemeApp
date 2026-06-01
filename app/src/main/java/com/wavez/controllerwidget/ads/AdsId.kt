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
            "ca-app-pub-5390451356176712/9922713350"
        }
    }

    fun getBannerAdsSplash(): String {
        return if (BuildConfig.DEBUG) {
            BANNER_TEST_ADS
        } else {
            "ca-app-pub-5390451356176712/5017871030"
        }
    }

    fun getBannerInAppAds(): String {
        return if (BuildConfig.DEBUG) {
            BANNER_TEST_ADS
        } else {
            "ca-app-pub-5390451356176712/1770102611"
        }
    }

    // --- Interstitial / Popup lists ---
    fun getListInterstitialAdsSplash(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(POPUP_TEST_ADS)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/1078626020",
                "ca-app-pub-5390451356176712/2551489793"
            )
        }
    }

    fun getListInterstitialInAppAds(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(POPUP_TEST_ADS)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/3357305005",
                "ca-app-pub-5390451356176712/4670386677"
            )
        }
    }

    // --- Native: language / onboarding / obd / home / config / select / in-app ---
    fun getListAdNativeLanguageAds1(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/9579932574",
                "ca-app-pub-5390451356176712/1893014245"
            )
        }
    }

    fun getListAdNativeLanguageAds2(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/2359918101",
                "ca-app-pub-5390451356176712/2200136002"
            )
        }
    }

    fun getListAdNativeOnboarding1(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/7229101409",
                "ca-app-pub-5390451356176712/5947809320"
            )
        }
    }

    fun getListAdNativeOnboarding1Click(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/1785203035",
                "ca-app-pub-5390451356176712/7069319304"
            )
        }
    }

    fun getListAdNativeOnboarding2(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/4219794683",
                "ca-app-pub-5390451356176712/4443155963"
            )
        }
    }

    fun getListAdNativeOnboarding3(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/7967468007",
                "ca-app-pub-5390451356176712/2791162947"
            )
        }
    }

    fun getNativeHomeAds(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf("ca-app-pub-5390451356176712/7296550018")
        }
    }

    fun getNativeInAppAds(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(NATIVE_AD_ID_TEST)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/1937623576")
        }
    }

    // --- Rewarded ---
    fun getRewardedSelectAds(): List<String> {
        return if (BuildConfig.DEBUG) {
            listOf(REWARD_TEST_ADS)
        } else {
            listOf(
                "ca-app-pub-5390451356176712/3335525213",
            )
        }
    }

    // --- Open ---
    fun getOpenAds(): String {
        return if (BuildConfig.DEBUG) {
            OPEN_UNIT_ID_TEST
        } else {
            "ca-app-pub-5390451356176712/5262926385"
        }
    }
}