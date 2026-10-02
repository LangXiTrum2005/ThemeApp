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

package thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.page

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.os.Bundle
import android.view.Gravity
import android.view.View
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.core.utils.CustomToast
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentOnboarding1Binding
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.WalkthroughActivity
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.adapter.HabitsAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.provider.HabitsProvider
import thinklife.prism.wallpaper.widget.theme.util.AppEvent
import thinklife.prism.wallpaper.widget.theme.util.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Onboarding1Fragment : BaseFragment<FragmentOnboarding1Binding>() {

    override val adsScreenId = AdsScreen.WALKTHROUGH_INTRO_VIEW


    override fun bindingView() = FragmentOnboarding1Binding.inflate(layoutInflater)

    companion object {
        fun newInstance() = Onboarding1Fragment()
    }

    private var isClickedFirst = false

    private val habitsAdapter by lazy { HabitsAdapter() }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        initHabits()
        FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_VIEW)
    }

    override fun initListener() {
        super.initListener()
        binding.btnNext.setOnClickListener {
            triggerAds(AdsTrigger.WALKTHROUGH_INTRO_CLICK_NEXT) {
                runWhenResumed {
                    if (habitsAdapter.getSelectedItems().isEmpty()) {
                        showSelectToast()
                    } else {
                        HabitsProvider.pushEvent(habitsAdapter.getSelectedItems())
                        FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_CLICK_NEXT)
                        (activity as WalkthroughActivity).nextScreen()
                    }
                }
            }
        }
    }

    private fun initHabits() {
        with(binding.viewObd1) {
            rcvHabits.adapter = habitsAdapter
            habitsAdapter.set(HabitsProvider.getHabits())
            habitsAdapter.onItemSelected = {
                if (!isClickedFirst) {
                    FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_CLICK)
                    isClickedFirst = true
                }
                binding.btnNext.isSelected = habitsAdapter.getSelectedItems().isNotEmpty()
            }
        }
    }

    private fun showSelectToast() {
        CustomToast.makeText(
            requireContext(),
            R.string.select_option,
            bgDrawable = R.drawable.bg_toas,
            gravity = Gravity.TOP,
            textColor = R.color.white,
        ).show()
    }
}
