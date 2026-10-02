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

package thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityWalkthroughBinding
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.adapter.OnboardingViewPagerAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.page.Onboarding1Fragment
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.page.WalkthroughDetailFragment
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.provider.OnboardingProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalkthroughActivity : BaseActivity<ActivityWalkthroughBinding>() {

    override val adsScreenId = AdsScreen.WALKTHROUGH_VIEW


    override fun bindingView(): ActivityWalkthroughBinding {
        return ActivityWalkthroughBinding.inflate(layoutInflater)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, WalkthroughActivity::class.java)
        }
    }

    private val fragments = ArrayList<Fragment>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initPage()
    }

    override fun initListener() {
        super.initListener()
        binding.imageViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val currentFragment = fragments.getOrNull(position)
                val isOnboardingDetail = currentFragment is WalkthroughDetailFragment
                        || currentFragment is Onboarding1Fragment
                binding.imageViewPager.isUserInputEnabled = !isOnboardingDetail
                binding.ivClose.isVisible = !isOnboardingDetail
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageScrollStateChanged(arg0: Int) {}
        })

        binding.ivClose.setOnClickListener {
            triggerAds(AdsTrigger.WALKTHROUGH_CLICK_IV_CLOSE) {
                runWhenResumed {
                    nextScreen()
                }
            }
        }
    }

    fun nextScreen() {
        if (binding.imageViewPager.currentItem == fragments.lastIndex) {
            sharedPref.isNeedGotoWalkthrough = false
            sharedPref.isNeedShowLanguage = false
            startActivity(MainActivity.newIntent(this))
            finish()

        } else {
            binding.imageViewPager.setCurrentItem(binding.imageViewPager.currentItem + 1, true)
        }
    }

    private fun initPage() {
        if (binding.imageViewPager.adapter != null) return
        fragments.add(Onboarding1Fragment.newInstance())
        fragments.add(WalkthroughDetailFragment.newInstance(OnboardingProvider.ONBOARD_02))
        fragments.add(WalkthroughDetailFragment.newInstance(OnboardingProvider.ONBOARD_03))
        binding.imageViewPager.adapter =
            OnboardingViewPagerAdapter(supportFragmentManager, lifecycle, fragments)
        binding.imageViewPager.isSaveEnabled = true
    }

    override fun onHandleBackPressed() {
        triggerBackAds(AdsTrigger.WALKTHROUGH_CLICK_BACK) { finish() }
    }

}