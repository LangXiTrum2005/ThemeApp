package com.wavez.themewallpaper.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPreferencesImpl(context: Context) : SharedPref {

    companion object {

        private const val TIME_OF_24_HOUR = 1 * 24 * 60 * 60 * 1000

        const val LANGUAGE_DEFAULT = ""

        private const val PREFERENCE_FILE_KEY = "shared_preferences_app_data"

        private const val APP_LANGUAGE = "app_language"

        private const val IS_NEED_SET_LANGUAGE = "is_need_set_language"

        private const val IS_FIRST_OPEN_APP = "is_first_open_app"

        private const val IS_RATE = "is_rate"

        private const val IS_AUTO_REMOVE = "is_auto_remove"

        private const val IS_VIP = "is_vip"


        private const val KEY_FIRST_TIME_OPEN_MAIN_APP = "first_time_open_main_app"


        private const val CAN_REQUEST_ADS = "can_request_ads"


        private const val IS_RATE_OPEN_FIRST_APP = "is_rate_open_first_app"

        private const val PITCH_VALUE = "pitch_value"

        private const val SPEED_VALUE = "speed_value"

        private const val IS_LOOPED = "is_looped"

        private const val IS_SOUND_EFFECT = "is_sound_effect"

        private const val IS_SHOW_INSTRUCTION = "is_show_instruction"

        private const val IS_SHOW_INSTRUCTION_GUESS = "is_show_instruction_guess"

        private const val LEVEL_PLAYED = "level_played"

        private const val LEVEL_PLAYED_GUESS = "level_played_guess"

        val TIME_RELOAD_AD_HOME = "time_reload_home"

        private const val KEY_APP_OPEN_COUNT = "app_open_count"

        private const val KEY_PERMISSION_ASK_COUNT = "permission_ask_count"

        private const val IS_SHOW_INSTRUCTION_PVP = "is_show_instruction_pvp"

    }

    override val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    override val editor: SharedPreferences.Editor
        get() = sharedPref.edit()

    override var appLanguage: String
        get() = sharedPref.getString(APP_LANGUAGE, LANGUAGE_DEFAULT) ?: ""
        set(value) {
            sharedPref.edit { putString(APP_LANGUAGE, value) }
        }

    override var isNeedSetLanguage: Boolean
        get() = sharedPref.getBoolean(IS_NEED_SET_LANGUAGE, true)
        set(value) {
            editor.putBoolean(IS_NEED_SET_LANGUAGE, value).apply()
        }

    override var isFirstOpenApp: Boolean
        get() = sharedPref.getBoolean(IS_FIRST_OPEN_APP, false)
        set(value) {
            editor.putBoolean(IS_FIRST_OPEN_APP, value).apply()
        }

    override var isRate: Boolean
        get() = sharedPref.getBoolean(IS_RATE, false)
        set(value) {
            editor.putBoolean(IS_RATE, value).apply()
        }

    override var isRateOpenFirstApp: Boolean
        get() = sharedPref.getBoolean(IS_RATE_OPEN_FIRST_APP, false)
        set(value) {
            sharedPref.edit { putBoolean(IS_RATE_OPEN_FIRST_APP, value) }
        }

    override var isAutoRemove: Boolean
        get() = sharedPref.getBoolean(IS_AUTO_REMOVE, false)
        set(value) {
            sharedPref.edit { putBoolean(IS_AUTO_REMOVE, value) }
        }

    override var isVip: Boolean
        get() = sharedPref.getBoolean(IS_VIP, false)

        set(value) {
            editor.putBoolean(IS_VIP, value).apply()
        }

    override var isFirstTimeOpenMainApp: Boolean
        get() = sharedPref.getBoolean(KEY_FIRST_TIME_OPEN_MAIN_APP, true)
        set(value) {
            editor.putBoolean(KEY_FIRST_TIME_OPEN_MAIN_APP, value).apply()
        }

    override var canRequestAds: Boolean
        get() = sharedPref.getBoolean(CAN_REQUEST_ADS, true)
        set(value) {
            editor.putBoolean(CAN_REQUEST_ADS, value).apply()
        }
    override var pitchValue: Float
        get() = sharedPref.getFloat(PITCH_VALUE, 1f)
        set(value) {
            editor.putFloat(PITCH_VALUE, value).apply()
        }
    override var speedValue: Float
        get() = sharedPref.getFloat(SPEED_VALUE, 1f)
        set(value) {
            editor.putFloat(SPEED_VALUE, value).apply()
        }

    override var isLoop: Boolean
        get() = sharedPref.getBoolean(IS_LOOPED, false)
        set(value) {
            editor.putBoolean(IS_LOOPED, value).apply()
        }

    override var isSoundEffect: Boolean
        get() = sharedPref.getBoolean(IS_SOUND_EFFECT, true)
        set(value) {
            editor.putBoolean(IS_SOUND_EFFECT, value).apply()
        }

    override var timeReloadAdHome: Long
        get() = sharedPref.getLong(TIME_RELOAD_AD_HOME, 10_000L)
        set(value) {
            editor.putLong(TIME_RELOAD_AD_HOME, value).apply()
        }

    override var isShowInstruction: Boolean
        get() = sharedPref.getBoolean(IS_SHOW_INSTRUCTION, true)
        set(value) {
            editor.putBoolean(IS_SHOW_INSTRUCTION, value).apply()
        }
    override var isShowInstructionGuess: Boolean
        get() = sharedPref.getBoolean(IS_SHOW_INSTRUCTION_GUESS, true)
        set(value) {
            editor.putBoolean(IS_SHOW_INSTRUCTION_GUESS, value).apply()
        }

    override var isShowInstructionPvP: Boolean
        get() = sharedPref.getBoolean(IS_SHOW_INSTRUCTION_PVP, true)
        set(value) {
            editor.putBoolean(IS_SHOW_INSTRUCTION_PVP, value).apply()
        }

    override fun getLevelPlayed(): Int {
        return sharedPref.getInt(LEVEL_PLAYED, 0)
    }

    override fun setLevelPlayer(level: Int) {
        editor.putInt(LEVEL_PLAYED, level).apply()
    }

    override fun getLevelPlayedGuess(): Int {
        return sharedPref.getInt(LEVEL_PLAYED_GUESS, 0)
    }

    override fun setLevelPlayerGuess(level: Int) {
        sharedPref.edit { putInt(LEVEL_PLAYED_GUESS, level) }
    }

    override fun increaseAppOpenCount() {
        val count = sharedPref.getInt(KEY_APP_OPEN_COUNT, 0)
        editor.putInt(KEY_APP_OPEN_COUNT, count + 1).apply()
    }

    override fun onPermissionDenied() {
        val askCount = sharedPref.getInt(KEY_PERMISSION_ASK_COUNT, 0)
        editor.putInt(KEY_PERMISSION_ASK_COUNT, askCount + 1).apply()
    }


    override fun shouldAskPermission(): Boolean {
        val openCount = sharedPref.getInt(KEY_APP_OPEN_COUNT, 0)
        val askCount = sharedPref.getInt(KEY_PERMISSION_ASK_COUNT, 0)
        if (openCount !in listOf(2, 3)) return false
        if (askCount >= 2) return false
        return true
    }

}
