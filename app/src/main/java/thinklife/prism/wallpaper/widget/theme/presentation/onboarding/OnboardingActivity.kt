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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import thinklife.prism.wallpaper.widget.theme.ads.AdsBootstrap
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPref
import thinklife.prism.wallpaper.widget.theme.data.provider.OnboardingProvider
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityOnboardingBinding
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.adapter.OnboardingPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Container onboarding: survey (onb1) -> 2 trang giới thiệu (onb2/onb3) -> welcome.
 * Mỗi trang con khai screenId riêng, Activity này chỉ giữ slot top/bottom của `onb_view`.
 */
@AndroidEntryPoint
class OnboardingActivity : BaseActivity<ActivityOnboardingBinding>() {


    override fun inflateBinding() = ActivityOnboardingBinding.inflate(layoutInflater)

    /** Container của pager — các trang con khai screenId riêng (onb1/onb2/onb3/welcome). */
    override val adsScreenId = AdsScreen.ONB_VIEW

    private val fragments = ArrayList<Fragment>()

    override fun initConfig(savedInstanceState: Bundle?) {
        initPage()
    }

    /** Kết thúc trang cuối: chốt FTUE rồi sang Main. */
    fun nextScreen() {
        if (binding.imageViewPager.currentItem == fragments.lastIndex) {
            sharedPref.isNeedGotoWalkthrough = false
            sharedPref.isNeedShowLanguage = false
            AdsBootstrap.markOnboardingCompleted()
            startActivity(MainActivity.newIntent(this))
            finish()
        } else {
            binding.imageViewPager.setCurrentItem(binding.imageViewPager.currentItem + 1, true)
        }
    }

    private fun initPage() {
        if (binding.imageViewPager.adapter != null) return

        fragments.add(SurveyFragment.newInstance())
        fragments.add(
            OnboardingPageFragment.newInstance(
                OnboardingProvider.onboardingList[0],
                pageIndex = OnboardingPageFragment.PAGE_ONB2,
            ),
        )
        fragments.add(
            OnboardingPageFragment.newInstance(
                OnboardingProvider.onboardingList[1],
                pageIndex = OnboardingPageFragment.PAGE_ONB3,
            ),
        )
        fragments.add(WelcomeFragment.newInstance())

        binding.imageViewPager.adapter =
            OnboardingPagerAdapter(supportFragmentManager, lifecycle, fragments)
        // Chỉ đi tới bằng nút Next — vuốt tay sẽ nhảy qua trigger ads của trang.
        binding.imageViewPager.isUserInputEnabled = false
        binding.imageViewPager.isSaveEnabled = true
    }

    /** LFO: chặn back, user phải đi hết onboarding. */
    override fun onHandleBackPressed() = Unit

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, OnboardingActivity::class.java)
    }
}
