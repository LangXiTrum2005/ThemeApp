package com.wavez.themewallpaper.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.wavez.themewallpaper.Secrets
import com.wavez.themewallpaper.ads.AppRemoteConfig
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

        private const val HAS_LOGGED_SPLASH_VIEW = "has_logged_splash_view"
        private const val HAS_LOGGED_HOME_VIEW = "has_logged_home_view"
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

    /**
     * Delegate for ad-show flags.
     * Getter: raw RC value AND !isVip — VIP users get false for every ad flag automatically.
     * Setter: persists the raw RC value so it survives VIP state changes.
     */
    private fun adFlag(key: String, default: Boolean = true): ReadWriteProperty<SharedPreferencesImpl, Boolean> =
        object : ReadWriteProperty<SharedPreferencesImpl, Boolean> {
            override fun getValue(thisRef: SharedPreferencesImpl, property: KProperty<*>): Boolean =
                sharedPref.getBoolean(key, default) && !thisRef.isVip

            override fun setValue(thisRef: SharedPreferencesImpl, property: KProperty<*>, value: Boolean) =
                sharedPref.edit { putBoolean(key, value) }
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
        get() = sharedPref.getString(SERVER_ROOT, Secrets.INSTANCE.getBaseURL()) ?: Secrets.INSTANCE.getBaseURL()
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
        get() = sharedPref.getLong(AppRemoteConfig.INTER_OPEN_ADS_GAP_KEY, 3_000L)
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
        get() = sharedPref.getLong(AppRemoteConfig.OPEN_ADS_GAP_KEY, 3_000L)
        set(value) {
            editor.putLong(AppRemoteConfig.OPEN_ADS_GAP_KEY, value).commit()
        }
    override var nativeGap: Long
        get() = sharedPref.getLong(AppRemoteConfig.NATIVE_GAP, 15_000L)
        set(value) {
            editor.putLong(AppRemoteConfig.NATIVE_GAP, value).commit()
        }


    override var lastTimeShowOpenAdSplash: Long
        get() = sharedPref.getLong(LAST_TIME_SHOW_OPEN_AD_SPLASH, 0)
        set(value) {
            editor.putLong(LAST_TIME_SHOW_OPEN_AD_SPLASH, value).apply()
        }

    override var shouldShowNativeLanguage1: Boolean by adFlag(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE, true)

    override var shouldShowNativeLanguage2: Boolean
            by adFlag(
            AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE2, true
        )

    override var shouldShowNativeObd1: Boolean
            by adFlag(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1, true)


    override var shouldShowNativeObd2: Boolean
            by adFlag(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_2, true)

    override var shouldShowNativeObd3: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_3, true)

    override var shouldShowNativeObd1Click: Boolean
         by adFlag(
            AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK,
            true
        )


    override var shouldShowNativeAdFull1: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_FULL_1, true)

    override var shouldShowNativeAdFull2: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_NATIVE_AD_FULL_2, true)

    override var shouldBannerSplash: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_BANNER_SPLASH, true)


    override var shouldShowInterSplash: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_INTER_SPLASH, true)

    override var shouldShowBannerHome: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_BANNER_HOME, true)



    override var shouldShowNativeHome: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_NATIVE_HOME, true)

    override var shouldShowNativeBoostSuccess: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_BOOST_SUCCESS, true)

    override var shouldShowNativeConfig: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_NATIVE_CONFIG, true)

    override var shouldShowNativeSelectApp: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_NATIVE_SELECT_APP, true)

    override var shouldShowReward: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_REWARD, true)

    override var shouldShowBannerInApp: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_BANNER_INAPP, true)

    override var shouldShowNativeInApp: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_NATIVE_INAPP, true)

    override var shouldShowInterInApp: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_INTER_INAPP, true)

    override var shouldShowOpenAd: Boolean
         by adFlag(AppRemoteConfig.SHOULD_SHOW_OPEN_AD, true)

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
