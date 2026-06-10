package com.wavez.themewallpaper.ui.onboarding.provider

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.wavez.themewallpaper.BuildConfig
import com.wavez.themewallpaper.MyApplication
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.ui.onboarding.model.Habits

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
        FirebaseAnalytics.getInstance(MyApplication.appContext()).logEvent("onb1_hobby", bundle)
    }

}