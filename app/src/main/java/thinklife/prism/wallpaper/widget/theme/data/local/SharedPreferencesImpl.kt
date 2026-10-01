/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.core.content.edit
import thinklife.prism.wallpaper.widget.theme.Secrets

class SharedPreferencesImpl(private val context: Context) : SharedPref {

    companion object {
        const val LANGUAGE_DEFAULT = ""

        private const val PREFERENCE_FILE_KEY = "shared_preferences_app_data"
        private const val APP_LANGUAGE = "app_language"
        private const val IS_NEED_SHOW_LANGUAGE = "is_need_show_language"
        private const val IS_NEED_GOTO_WALKTHROUGH = "is_need_goto_walkthrough"
        private const val KEY_HAS_OPENED_BEFORE = "has_opened_before"
        private const val IS_FIRST_OPEN_APP = "is_first_open_app"
        private const val IS_RATE = "is_rate"
        private const val RATED_STARS = "rated_stars"
        private const val RATE_DIALOG_SHOW_COUNT = "rate_dialog_show_count"
        private const val IS_RATE_DIALOG_PENDING_FROM_SPLASH = "is_rate_dialog_pending_from_splash"
        private const val IS_AUTO_REMOVE = "is_auto_remove"
        private const val KEY_FIRST_TIME_OPEN_MAIN_APP = "first_time_open_main_app"
        private const val IS_RATE_OPEN_FIRST_APP = "is_rate_open_first_app"
        private const val LEVEL_PLAYED_GUESS = "level_played_guess"
        private const val KEY_APP_OPEN_COUNT = "app_open_count"
        private const val KEY_PERMISSION_ASK_COUNT = "permission_ask_count"
        private const val SERVER_ROOT = "SERVER_ROOT"
        private const val ACTIVE_THEME_ID = "active_theme_id"
        private const val ADDED_THEME_WIDGET_SIZES_PREFIX = "added_theme_widget_sizes_"
        private const val HAS_LOGGED_SPLASH_VIEW = "has_logged_splash_view"
        private const val HAS_LOGGED_HOME_VIEW = "has_logged_home_view"
    }

    override val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    override val editor: SharedPreferences.Editor
        get() = sharedPref.edit()

    override fun getConfigLanguage(): String = sharedPref.getString(APP_LANGUAGE, LANGUAGE_DEFAULT) ?: LANGUAGE_DEFAULT

    override fun setConfigLanguage(language: String) {
        editor.putString(APP_LANGUAGE, language).apply()
    }

    override val deviceId: String
        @SuppressLint("HardwareIds")
        get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""

    override var isNeedShowLanguage: Boolean
        get() = sharedPref.getBoolean(IS_NEED_SHOW_LANGUAGE, true)
        set(value) {
            editor.putBoolean(IS_NEED_SHOW_LANGUAGE, value).apply()
        }

    override var isNeedGotoWalkthrough: Boolean
        get() = sharedPref.getBoolean(IS_NEED_GOTO_WALKTHROUGH, true)
        set(value) {
            editor.putBoolean(IS_NEED_GOTO_WALKTHROUGH, value).apply()
        }

    override var hasOpenedBefore: Boolean
        get() = sharedPref.getBoolean(KEY_HAS_OPENED_BEFORE, false)
        set(value) {
            editor.putBoolean(KEY_HAS_OPENED_BEFORE, value).apply()
        }

    override var isFirstOpenApp: Boolean
        get() = sharedPref.getBoolean(IS_FIRST_OPEN_APP, false)
        set(value) {
            editor.putBoolean(IS_FIRST_OPEN_APP, value).apply()
        }

    override var severRoot: String
        get() = sharedPref.getString(SERVER_ROOT, Secrets.INSTANCE.getBaseURL()) ?: Secrets.INSTANCE.getBaseURL()
        set(value) {
            editor.putString(SERVER_ROOT, value).apply()
        }

    override var isRate: Boolean
        get() = sharedPref.getBoolean(IS_RATE, false)
        set(value) {
            editor.putBoolean(IS_RATE, value).apply()
        }

    override var ratedStars: Int
        get() = sharedPref.getInt(RATED_STARS, 0)
        set(value) {
            editor.putInt(RATED_STARS, value.coerceIn(0, 5)).apply()
        }

    override var rateDialogShowCount: Int
        get() = sharedPref.getInt(RATE_DIALOG_SHOW_COUNT, 0)
        set(value) {
            editor.putInt(RATE_DIALOG_SHOW_COUNT, value).apply()
        }

    override var isRateDialogPendingFromSplash: Boolean
        get() = sharedPref.getBoolean(IS_RATE_DIALOG_PENDING_FROM_SPLASH, false)
        set(value) {
            editor.putBoolean(IS_RATE_DIALOG_PENDING_FROM_SPLASH, value).apply()
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

    override var isFirstTimeOpenMainApp: Boolean
        get() = sharedPref.getBoolean(KEY_FIRST_TIME_OPEN_MAIN_APP, true)
        set(value) {
            editor.putBoolean(KEY_FIRST_TIME_OPEN_MAIN_APP, value).apply()
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

    override var activeThemeId: String?
        get() = sharedPref.getString(ACTIVE_THEME_ID, null)
        set(value) {
            sharedPref.edit { putString(ACTIVE_THEME_ID, value) }
        }

    override fun markThemeWidgetSizeAdded(themeId: String, size: String) {
        val key = ADDED_THEME_WIDGET_SIZES_PREFIX + themeId
        val sizes = sharedPref.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
        sizes.add(size)
        sharedPref.edit { putStringSet(key, sizes) }
    }

    override fun getAddedThemeWidgetSizes(themeId: String): Set<String> {
        return sharedPref.getStringSet(ADDED_THEME_WIDGET_SIZES_PREFIX + themeId, emptySet())
            ?: emptySet()
    }

    override var hasLoggedSplashView: Boolean
        get() = sharedPref.getBoolean(HAS_LOGGED_SPLASH_VIEW, false)
        set(value) {
            editor.putBoolean(HAS_LOGGED_SPLASH_VIEW, value).apply()
        }

    override var hasLoggedHomeView: Boolean
        get() = sharedPref.getBoolean(HAS_LOGGED_HOME_VIEW, false)
        set(value) {
            editor.putBoolean(HAS_LOGGED_HOME_VIEW, value).apply()
        }
}
