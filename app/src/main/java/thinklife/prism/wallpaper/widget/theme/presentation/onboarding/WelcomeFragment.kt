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

package thinklife.prism.wallpaper.widget.theme.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentWelcomeBinding

class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentWelcomeBinding.inflate(inflater, container, false)

    companion object {
        fun newInstance() = WelcomeFragment()
    }

    override val adsScreenId = AdsScreen.WELCOME_VIEW

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        Glide.with(this)
            .load(R.drawable.obd_img_welcome)
            .placeholder(R.drawable.obd_img_welcome)
            .into(binding.ivImage)
    }

    override fun initListener() {
        super.initListener()
        binding.btnNext.setOnClickListener {
            triggerAds(AdsTrigger.WELCOME_CLICK_NEXT) {
                runWhenResumed { (activity as? OnboardingActivity)?.nextScreen() }
            }
        }
    }
}
