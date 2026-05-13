package com.wavez.themewallpaper.data.repository

import android.content.SharedPreferences

interface SharedPref {

    val sharedPref: SharedPreferences

    val editor: SharedPreferences.Editor

    var appLanguage: String

    var isNeedSetLanguage: Boolean

    var isFirstOpenApp: Boolean

    var isRate: Boolean

    var isRateOpenFirstApp: Boolean

    var isAutoRemove: Boolean

    var isVip: Boolean

    var isFirstTimeOpenMainApp: Boolean

    var canRequestAds: Boolean

    var pitchValue: Float

    var speedValue: Float

    var isLoop: Boolean

    var isSoundEffect: Boolean

    var timeReloadAdHome: Long

    var isShowInstruction: Boolean

    var isShowInstructionGuess: Boolean

    var isShowInstructionPvP: Boolean

    fun getLevelPlayed(): Int

    fun setLevelPlayer(level: Int)

    fun getLevelPlayedGuess(): Int

    fun setLevelPlayerGuess(level: Int)

    fun increaseAppOpenCount()

    fun shouldAskPermission(): Boolean

    fun onPermissionDenied()
}