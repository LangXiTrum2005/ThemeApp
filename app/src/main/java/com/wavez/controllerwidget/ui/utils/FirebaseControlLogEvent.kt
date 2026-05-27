package com.wavez.controllerwidget.ui.utils

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

object FirebaseControlLogEvent {
    fun logEvent(event: AppEvent, params: Bundle? = null) {
        Firebase.analytics.logEvent(event.value, params)
    }
}

enum class AppEvent(val value: String) {
    SPLASH_1ST_VIEW("splash_1st_view"),
    SPLASH_REOPEN_VIEW("splash_reopen_view"),
    LFO_VIEW("lfo_view"),
    LFO_CLICK_LANGUAGE("lfo_click_language"),
    LFO_CLICK_NEXT("lfo_click_next"),
    ONBOARDING_1_VIEW("onboarding_1_view"),
    ONBOARDING_1_CLICK("onboarding_1_click"),
    ONBOARDING_1_CLICK_NEXT("onboarding_1_click_next"),
    ONBOARDING_2_VIEW("onboarding_2_view"),
    ONBOARDING_2_CLICK_NEXT("onboarding_2_click_next"),
    ONBOARDING_3_VIEW("onboarding_3_view"),
    ONBOARDING_3_CLICK_NEXT("onboarding_3_click_next"),
    WELCOME_VIEW("welcome_view"),
    WELCOME_CLICK("welcome_click"),
    HOME_1ST_VIEW("home_1st_view"),
    HOME_REOPEN_VIEW("home_reopen_view"),
    HOME_CLICK_TURBO_MODE("home_click_turbo_mode"),
    HOME_CLICK_OPTIMAL_MODE("home_click_optimal_mode"),
    HOME_CLICK_POWER_SAVE_MODE("home_click_power_save_mode"),
    HOME_CLICK_ADD_GAME("home_click_add_game"),
    HOME_CLICK_ADD("home_click_add"),
    HOME_CLICK_PLAY("home_click_play"),
    HOME_CLICK_SETTING("home_click_setting"),
    USER_SELECT_APP_CLICK_SAVE("user_select_app_click_save"),
    USER_SELECT_APP_VIEW_SUCCESS("user_select_app_view_success"),
    USER_CONFIG_CLICK_SAVE("user_config_click_save"),
    USER_CONFIG_VIEW_SUCCESS("user_config_view_success"),
}
