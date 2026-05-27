package com.wavez.controllerwidget.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.wavez.controllerwidget.ads.FullScreenNativeAdFragment
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.databinding.ActivityWalkthroughBinding
import com.wavez.controllerwidget.ui.home.MainActivity
import com.wavez.controllerwidget.ui.onboarding.adapter.OnboardingViewPagerAdapter
import com.wavez.controllerwidget.ui.onboarding.page.WalkthroughDetailFragment
import com.wavez.controllerwidget.ui.onboarding.provider.OnboardingProvider
import com.wavez.controllerwidget.ui.premium.PremiumActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WalkthroughActivity : BaseActivity<ActivityWalkthroughBinding>() {

    override fun bindingView(): ActivityWalkthroughBinding {
        return ActivityWalkthroughBinding.inflate(layoutInflater)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, WalkthroughActivity::class.java)
        }
    }

    private val fragments = ArrayList<Fragment>()

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        lifecycleScope.launch {
            loadNativeWithPosition(NativeAdPlacement.ONBOARDING_FULL_1, reloadToClick = true)
        }
    }

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
                if (currentFragment is FullScreenNativeAdFragment) {
                    currentFragment.runWhenViewResumed {
                        currentFragment.onFragmentVisible()
                    }
                }
                val isOnboardingDetail = currentFragment is WalkthroughDetailFragment
                binding.imageViewPager.isUserInputEnabled = !isOnboardingDetail
                binding.ivClose.isVisible = !isOnboardingDetail
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageScrollStateChanged(arg0: Int) {}
        })

        binding.ivClose.setOnClickListener {
            nextScreen()
        }
    }

    fun nextScreen() {
        if (binding.imageViewPager.currentItem == fragments.lastIndex) {
            sharedPref.isNeedGotoWalkthrough = false
            sharedPref.isNeedShowLanguage = false
            if (sharedPref.isVip) {
                startActivity(MainActivity.newIntent(this))
            } else {
                startActivity(PremiumActivity.newIntent(this, false))
            }
            finish()

        } else {
            binding.imageViewPager.setCurrentItem(binding.imageViewPager.currentItem + 1, true)
        }
    }

    private fun initPage() {
        if (binding.imageViewPager.adapter != null) return

        fragments.add(
            WalkthroughDetailFragment.newInstance(
                OnboardingProvider.ONBOARD_01, sharedPref.shouldShowNativeObd1
            )
        )
        fragments.add(
            WalkthroughDetailFragment.newInstance(
                OnboardingProvider.ONBOARD_02
            )
        )
        if (sharedPref.shouldShowNativeAdFull1 && !sharedPref.isVip) {
            fragments.add(FullScreenNativeAdFragment.newInstance(NativeAdPlacement.ONBOARDING_FULL_1))
        }
        fragments.add(
            WalkthroughDetailFragment.newInstance(
                OnboardingProvider.ONBOARD_03
            )
        )
        if (sharedPref.shouldShowNativeAdFull2 && !sharedPref.isVip) {
            fragments.add(FullScreenNativeAdFragment.newInstance(NativeAdPlacement.ONBOARDING_FULL_2))
        }

        binding.imageViewPager.adapter =
            OnboardingViewPagerAdapter(supportFragmentManager, lifecycle, fragments)
        binding.imageViewPager.isSaveEnabled = true
    }

}