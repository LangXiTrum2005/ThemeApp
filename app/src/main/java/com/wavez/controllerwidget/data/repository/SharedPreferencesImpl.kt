package com.wavez.controllerwidget.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.wavez.controllerwidget.BuildConfig
import com.wavez.controllerwidget.ads.AppRemoteConfig

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

        private const val LEVEL_PLAYED_GUESS = "level_played_guess"

        private const val KEY_APP_OPEN_COUNT = "app_open_count"

        private const val KEY_PERMISSION_ASK_COUNT = "permission_ask_count"

        private const val SERVER_ROOT = "SERVER_ROOT"

        private const val LAST_TIME_SHOW_OPEN_AD_SPLASH = "last_time_show_open_ad_splash"

        private const val ACTIVE_THEME_ID = "active_theme_id"

        private const val UNLOCKED_VIP_WALLPAPER_IDS = "unlocked_vip_wallpaper_ids"

        private const val UNLOCKED_THEME_WALLPAPER_IDS = "unlocked_theme_wallpaper_ids"

        private const val UNLOCKED_ICON_IDS = "unlocked_icon_ids"

        private const val IS_NEED_SHOW_LANGUAGE = "is_need_show_language"

        private const val IS_NEED_GOTO_WALKTHROUGH = "is_need_goto_walkthrough"
    }

    override val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    override val editor: SharedPreferences.Editor
        get() = sharedPref.edit()

    override fun getConfigLanguage(): String {
        return sharedPref.getString(APP_LANGUAGE, LANGUAGE_DEFAULT) ?: LANGUAGE_DEFAULT
    }

    override fun setConfigLanguage(language: String) {
        editor.putString(APP_LANGUAGE, language).apply()
    }

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

    override var isFirstOpenApp: Boolean
        get() = sharedPref.getBoolean(IS_FIRST_OPEN_APP, false)
        set(value) {
            editor.putBoolean(IS_FIRST_OPEN_APP, value).apply()
        }

    override var severRoot: String
        get() = sharedPref.getString(SERVER_ROOT, BuildConfig.BASE_URL) ?: BuildConfig.BASE_URL
        set(value) {
            editor.putString(SERVER_ROOT, value).apply()
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


    override var interOpenAdsGap: Long
        get() = sharedPref.getLong(AppRemoteConfig.INTER_OPEN_ADS_GAP_KEY, 5_000L)
        set(value) {
            editor.putLong(AppRemoteConfig.INTER_OPEN_ADS_GAP_KEY, value).commit()
        }

    override var nativeAdPreloadCount: Int
        get() = sharedPref.getInt(AppRemoteConfig.NATIVE_AD_PRELOAD_COUNT_KEY, 1)
        set(value) {
            editor.putInt(AppRemoteConfig.NATIVE_AD_PRELOAD_COUNT_KEY, value).commit()
        }

    override var interAdsGap: Long
        get() = sharedPref.getLong(AppRemoteConfig.INTER_ADS_GAP_KEY, 30_000L)
        set(value) {
            editor.putLong(AppRemoteConfig.INTER_ADS_GAP_KEY, value).commit()
        }

    override var openAdsGap: Long
        get() = sharedPref.getLong(AppRemoteConfig.OPEN_ADS_GAP_KEY, 5_000L)
        set(value) {
            editor.putLong(AppRemoteConfig.OPEN_ADS_GAP_KEY, value).commit()
        }


    override var lastTimeShowOpenAdSplash: Long
        get() = sharedPref.getLong(LAST_TIME_SHOW_OPEN_AD_SPLASH, 0)
        set(value) {
            editor.putLong(LAST_TIME_SHOW_OPEN_AD_SPLASH, value).apply()
        }

    override var shouldShowNativeLanguage1: Boolean
        get() = sharedPref.getBoolean(
            AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE, true
        )
        set(value) = sharedPref.edit {
            putBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE, value)
        }

    override var shouldShowNativeLanguage2: Boolean
        get() = sharedPref.getBoolean(
            AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE2, true
        )
        set(value) {
            sharedPref.edit {
                putBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE2, value)
            }
        }

    override var shouldShowNativeObd1: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1, true)
        set(value) = sharedPref.edit {
            putBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1, value)
        }

    override var shouldShowNativeObd1Click: Boolean
        get() = sharedPref.getBoolean(
            AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK,
            true
        )
        set(value) {
            sharedPref.edit {
                putBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK, value)
            }
        }

    override var shouldShowNativeAdFull1: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_FULL_1, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_FULL_1, value) }
        }

    override var shouldShowNativeAdFull2: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_FULL_2, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_FULL_2, value) }
        }

    override var shouldBannerSplash: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_BANNER_SPLASH, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_BANNER_SPLASH, value) }
        }

    override var shouldShowInterSplash: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_INTER_SPLASH, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_INTER_SPLASH, value) }
        }
    override var shouldShowBannerHome: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_BANNER_HOME, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_BANNER_HOME, value) }
        }
    override var shouldShowNativeHome: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_HOME, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_HOME, value) }
        }
    override var shouldShowNativeBoostSuccess: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_BOOST_SUCCESS, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_BOOST_SUCCESS, value) }
        }
    override var shouldShowNativeConfig: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_CONFIG, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_CONFIG, value) }
        }
    override var shouldShowNativeSelectApp: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_SELECT_APP, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_SELECT_APP, value) }
        }


    override var shouldShowRewardSelect: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_REWARD_SELECT, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_REWARD_SELECT, value) }
        }

    override var shouldShowRewardConfig: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_REWARD_CONFIG, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_REWARD_CONFIG, value) }
        }

    override var shouldShowBannerInApp: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_BANNER_INAPP, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_BANNER_INAPP, value) }
        }
    override var shouldShowNativeInApp: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_INAPP, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_NATIVE_INAPP, value) }
        }
    override var shouldShowInterInApp: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_INTER_INAPP, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_INTER_INAPP, value) }
        }
    override var shouldShowOpenAd: Boolean
        get() = sharedPref.getBoolean(AppRemoteConfig.SHOULD_SHOW_OPEN_AD, true)
        set(value) {
            sharedPref.edit { putBoolean(AppRemoteConfig.SHOULD_SHOW_OPEN_AD, value) }
        }

    override var activeThemeId: String?
        get() = sharedPref.getString(ACTIVE_THEME_ID, null)
        set(value) {
            sharedPref.edit { putString(ACTIVE_THEME_ID, value) }
        }

    override fun isWallpaperUnlocked(wallpaperId: String): Boolean {
        return sharedPref.getStringSet(UNLOCKED_VIP_WALLPAPER_IDS, emptySet())
            ?.contains(wallpaperId) == true
    }

    override fun unlockWallpaper(wallpaperId: String) {
        val unlockedIds = sharedPref.getStringSet(UNLOCKED_VIP_WALLPAPER_IDS, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()
        unlockedIds.add(wallpaperId)
        sharedPref.edit { putStringSet(UNLOCKED_VIP_WALLPAPER_IDS, unlockedIds) }
    }

    override fun isThemeWallpaperUnlocked(wallpaperId: String): Boolean {
        return sharedPref.getStringSet(UNLOCKED_THEME_WALLPAPER_IDS, emptySet())
            ?.contains(wallpaperId) == true
    }

    override fun unlockThemeWallpaper(wallpaperId: String) {
        val unlockedIds = sharedPref.getStringSet(UNLOCKED_THEME_WALLPAPER_IDS, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()
        unlockedIds.add(wallpaperId)
        sharedPref.edit { putStringSet(UNLOCKED_THEME_WALLPAPER_IDS, unlockedIds) }
    }

    override fun isIconUnlocked(iconId: String): Boolean {
        return sharedPref.getStringSet(UNLOCKED_ICON_IDS, emptySet())
            ?.contains(iconId) == true
    }

    override fun unlockIcon(iconId: String) {
        val unlockedIds = sharedPref.getStringSet(UNLOCKED_ICON_IDS, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()
        unlockedIds.add(iconId)
        sharedPref.edit { putStringSet(UNLOCKED_ICON_IDS, unlockedIds) }
    }

}
