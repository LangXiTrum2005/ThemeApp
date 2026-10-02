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

package thinklife.prism.wallpaper.widget.theme.presentation.setting.dialog

import androidx.fragment.app.FragmentManager
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPref

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
