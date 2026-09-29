package com.wavez.themewallpaper.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.databinding.ActivityWalkthroughBinding
import com.wavez.themewallpaper.ui.home.MainActivity
import com.wavez.themewallpaper.ui.onboarding.adapter.OnboardingViewPagerAdapter
import com.wavez.themewallpaper.ui.onboarding.page.Onboarding1Fragment
import com.wavez.themewallpaper.ui.onboarding.page.WalkthroughDetailFragment
import com.wavez.themewallpaper.ui.onboarding.provider.OnboardingProvider
import dagger.hilt.android.AndroidEntryPoint

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
            nextScreen()
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

}