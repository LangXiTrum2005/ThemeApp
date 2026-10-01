package com.wavez.themewallpaper.data.repository

import android.content.SharedPreferences

interface SharedPref {

    val sharedPref: SharedPreferences

    val editor: SharedPreferences.Editor

    fun getConfigLanguage(): String

    fun setConfigLanguage(language: String)

    var severRoot: String

    var isNeedShowLanguage: Boolean

    var isNeedGotoWalkthrough: Boolean

    var isFirstOpenApp: Boolean

    var isRate: Boolean

    var ratedStars: Int

    var rateDialogShowCount: Int

    var isRateDialogPendingFromSplash: Boolean

    var isRateOpenFirstApp: Boolean

    var isAutoRemove: Boolean

    var isFirstTimeOpenMainApp: Boolean

    fun setLevelPlayerGuess(level: Int)

    fun increaseAppOpenCount()

    fun shouldAskPermission(): Boolean

    fun onPermissionDenied()

    // ID of the controller theme currently applied by the user. Null = use built-in default.
    var activeThemeId: String?

    fun markThemeWidgetSizeAdded(themeId: String, size: String)

    fun getAddedThemeWidgetSizes(themeId: String): Set<String>

    var hasLoggedSplashView: Boolean
    var hasLoggedHomeView: Boolean
}
