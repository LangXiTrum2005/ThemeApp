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

package com.onetabb.template.android.presentation.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import com.onetabb.template.android.ads.AdsScreen
import com.onetabb.template.android.ads.AdsTrigger
import com.onetabb.template.android.core.base.BaseFragment
import com.onetabb.template.android.databinding.FragmentWelcomeBinding

class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentWelcomeBinding.inflate(inflater, container, false)

    companion object {
        fun newInstance() = WelcomeFragment()
    }

    override val adsScreenId = AdsScreen.WELCOME_VIEW

    override fun initListener() {
        super.initListener()
        binding.btnNext.setOnClickListener {
            triggerAds(AdsTrigger.WELCOME_CLICK_NEXT) {
                runWhenResumed { (activity as? OnboardingActivity)?.nextScreen() }
            }
        }
    }
}
