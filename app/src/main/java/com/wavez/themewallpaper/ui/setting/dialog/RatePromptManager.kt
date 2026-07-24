package com.wavez.themewallpaper.ui.setting.dialog

import androidx.fragment.app.FragmentManager
import com.wavez.themewallpaper.data.repository.SharedPref

object RatePromptManager {

    private const val MAX_SHOW_COUNT = 2

    fun prepareMainPromptAfterSplash(sharedPref: SharedPref) {
        sharedPref.isRateDialogPendingFromSplash =
            !sharedPref.isRate && sharedPref.rateDialogShowCount == 1
    }

    fun showAfterSuccessfulSetup(
        fragmentManager: FragmentManager,
        sharedPref: SharedPref,
    ) {
        if (sharedPref.rateDialogShowCount != 0) return
        show(fragmentManager, sharedPref)
    }

    fun showOnMainFromSplash(
        fragmentManager: FragmentManager,
        sharedPref: SharedPref,
    ) {
        if (!sharedPref.isRateDialogPendingFromSplash) return
        sharedPref.isRateDialogPendingFromSplash = false
        show(fragmentManager, sharedPref)
    }

    private fun show(
        fragmentManager: FragmentManager,
        sharedPref: SharedPref,
    ) {
        if (sharedPref.isRate || sharedPref.rateDialogShowCount >= MAX_SHOW_COUNT) return
        if (fragmentManager.isStateSaved) return
        if (fragmentManager.findFragmentByTag(RateDialogFragment.TAG) != null) return

        sharedPref.rateDialogShowCount += 1
        RateDialogFragment.newInstance().show(fragmentManager, RateDialogFragment.TAG)
    }
}
