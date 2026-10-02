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

package thinklife.prism.wallpaper.widget.theme.util

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
    LFO_VIEW("language_screen_view"),
    LFO_CLICK_LANGUAGE("click_language"),
    LFO_CLICK_NEXT("click_btn_next"),
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
}
