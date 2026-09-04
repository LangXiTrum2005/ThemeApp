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

package com.onetabb.template.android.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings

class SharedPreferencesImpl(private val context: Context) : SharedPref {

    companion object {
        const val LANGUAGE_DEFAULT = ""

        private const val PREFERENCE_FILE_KEY = "shared_preferences_app_data"
        private const val APP_LANGUAGE = "app_language"
        private const val IS_NEED_SHOW_LANGUAGE = "is_need_show_language"
        private const val IS_NEED_GOTO_WALKTHROUGH = "is_need_goto_walkthrough"
        private const val KEY_HAS_OPENED_BEFORE = "has_opened_before"
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
}
