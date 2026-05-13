package com.wavez.controllerwidget.data.repository

import android.content.SharedPreferences

interface SharedPref {

    val sharedPref: SharedPreferences

    val editor: SharedPreferences.Editor

    var appLanguage: String

    var severRoot : String

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
}