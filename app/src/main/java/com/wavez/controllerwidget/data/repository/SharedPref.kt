package com.wavez.controllerwidget.data.repository

import android.content.SharedPreferences

interface SharedPref {

    val sharedPref: SharedPreferences

    val editor: SharedPreferences.Editor

    fun getConfigLanguage(): String

    fun setConfigLanguage(language: String)

    var severRoot: String

    var isNeedSetLanguage: Boolean

    var isFirstOpenApp: Boolean

    var isRate: Boolean

    var isRateOpenFirstApp: Boolean

    var isAutoRemove: Boolean

    var isVip: Boolean

    var isFirstTimeOpenMainApp: Boolean
    var canRequestAds: Boolean

    fun setLevelPlayerGuess(level: Int)

    fun increaseAppOpenCount()

    fun shouldAskPermission(): Boolean

    fun onPermissionDenied()

    ///ads

    var interOpenAdsGap: Long

    var nativeAdPreloadCount: Int

    var interAdsGap: Long

    var openAdsGap: Long

    var lastTimeShowOpenAdSplash : Long

    var shouldShowNativeLanguage1: Boolean
    var shouldShowNativeLanguage2: Boolean

    var shouldShowNativeObd1Click : Boolean
    var shouldShowNativeObd1 : Boolean
    var shouldShowNativeAdFull1: Boolean
    var shouldShowNativeAdFull2: Boolean
    var shouldBannerSplash : Boolean
    var shouldShowInterSplash : Boolean

    var shouldShowBannerHome : Boolean
    var shouldShowNativeHome : Boolean
    var shouldShowNativeSelectApp : Boolean
    var shouldShowNativeBoostSuccess : Boolean
    var shouldShowNativeConfig : Boolean

    var shouldShowRewardSelect : Boolean
    var shouldShowRewardConfig : Boolean

    var shouldShowBannerInApp : Boolean
    var shouldShowNativeInApp : Boolean
    var shouldShowInterInApp : Boolean
    var shouldShowOpenAd : Boolean

    // ID of the controller theme currently applied by the user. Null = use built-in default.
    var activeThemeId: String?

    fun isWallpaperUnlocked(wallpaperId: String): Boolean

    fun unlockWallpaper(wallpaperId: String)
}
