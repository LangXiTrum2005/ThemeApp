package com.wavez.themewallpaper.ads

interface AppRemoteConfig {

    companion object {

        const val INTER_OPEN_ADS_GAP_KEY = "inter_open_ads_gap"

        const val OPEN_ADS_GAP_KEY = "open_ads_gap"

        const val INTER_ADS_GAP_KEY = "ads_gap_inter"

        const val NATIVE_GAP = "ads_gap_native"

        const val SERVER_ROOT = "server_root"

        const val NATIVE_AD_PRELOAD_COUNT_KEY = "native_ad_preload_count"
        const val SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE =
            "native_language_1_on_off"
        const val SHOULD_SHOW_NATIVE_AD_ON_FIRST_SETTING_LANGUAGE2 =
            "native_language_2_on_off"

        const val SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1 =
            "native_onboarding_1_on_off"
        const val SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_1_CLICK =
            "native_onb1_click_on_off"
        const val SHOULD_SHOW_NATIVE_AD_FULL_1 = "native_full_1_on_off"
        const val SHOULD_SHOW_NATIVE_AD_FULL_2 = "native_full_2_on_off"

        const val SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_2 = "native_onboarding_2_on_off"
        const val SHOULD_SHOW_NATIVE_AD_ON_WALKTHROUGH_3 = "native_onboarding_3_on_off"

        const val SHOULD_SHOW_NATIVE_AD_ON_SETTING_LANGUAGE =
            "should_show_native_ad_on_setting_language"
        const val SHOULD_SHOW_BANNER_SPLASH = "banner_splash_on_off"

        const val SHOULD_SHOW_INTER_SPLASH = "inter_splash_on_off"

        const val SHOULD_SHOW_BANNER_HOME = "banner_home_on_off"
        const val SHOULD_SHOW_NATIVE_HOME = "native_home_on_off"
        const val SHOULD_SHOW_BOOST_SUCCESS = "native_boost_success_on_off"
        const val SHOULD_SHOW_NATIVE_CONFIG = "native_config_on_off"
        const val SHOULD_SHOW_NATIVE_SELECT_APP = "native_icon_select_app_on_off"
        const val SHOULD_SHOW_REWARD = "ad_reward_on_off"
        const val SHOULD_SHOW_BANNER_INAPP = "banner_inapp_on_off"
        const val SHOULD_SHOW_NATIVE_INAPP = "native_inapp_on_off"
        const val SHOULD_SHOW_INTER_INAPP = "inter_inapp_on_off"
        const val SHOULD_SHOW_OPEN_AD = "open_ad_on_off"

    }

    fun fetchRemoteConfig()

}