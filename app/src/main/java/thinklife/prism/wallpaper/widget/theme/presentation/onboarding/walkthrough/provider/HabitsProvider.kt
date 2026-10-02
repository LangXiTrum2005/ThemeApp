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

package thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.provider

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import thinklife.prism.wallpaper.widget.theme.BuildConfig
import thinklife.prism.wallpaper.widget.theme.App
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.model.Habits

object HabitsProvider {

    private val habits = listOf<Habits>(
        Habits(
            1, R.string.anime, R.drawable.obd_img_anime, icon = R.drawable.ic_anime, "onb1_anime"
        ),
        Habits(
            2,
            R.string.aesthetics,
            R.drawable.obd_img_aesthetics,
            icon = R.drawable.ic_aesthetics,
            "onb1_aesthetics"
        ),
        Habits(3, R.string.cute, R.drawable.obd_img_cute, icon = R.drawable.ic_cute, "onb1_cute"),
        Habits(4, R.string.kpop, R.drawable.obd_img_kpop, icon = R.drawable.kpop, "onb1_kpop"),
    )

    fun getHabits(): List<Habits> {
        return habits
    }

    fun pushEvent(data: List<Habits>) {
        if (data.isNotEmpty()) {
            data.forEach {
                logEventHabit(it.event)
            }
        }
    }

    private fun logEventHabit(name: String) {
        if (BuildConfig.DEBUG) {
            return
        }
        val bundle = Bundle().apply {
            putString("hobby_name", name)
        }
        FirebaseAnalytics.getInstance(App.appContext()).logEvent("onb1_hobby", bundle)
    }

}