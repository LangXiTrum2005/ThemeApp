package com.wavez.themewallpaper.ui.home.fragment.mygallery

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.wavez.themewallpaper.core.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.databinding.FragmentMyGalleryBinding
import com.wavez.themewallpaper.ui.customview.ThemeTabIcon
import com.wavez.themewallpaper.ui.home.fragment.mygallery.adapter.MyGalleryPagerAdapter
import com.wavez.themewallpaper.ui.premium.PremiumActivity
import com.wavez.themewallpaper.ui.setting.SettingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyGalleryFragment : BaseFragment<FragmentMyGalleryBinding>() {
    override fun bindingView(): FragmentMyGalleryBinding {
        return FragmentMyGalleryBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance(): MyGalleryFragment {
            return MyGalleryFragment()
        }
    }

    private var tabMediator: TabLayoutMediator? = null
    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateTabState(position)
        }
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.my_gallery)
        setupPager()
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.btnPremium.isVisible = !sharedPref.isVip
    }


    override fun initListener() {
        super.initListener()
        binding.toolbar.btnSetting.setOnClickListener {
            startActivity(SettingActivity.newIntent(requireContext()))
        }

        binding.toolbar.btnPremium.setOnClickListener {
            startActivity(PremiumActivity.newIntent(requireContext()))
        }
    }

    private fun setupPager() {
        binding.vpGallery.adapter = MyGalleryPagerAdapter(this)
        binding.vpGallery.registerOnPageChangeCallback(onPageChangeCallback)
        tabMediator = TabLayoutMediator(binding.tabCategory, binding.vpGallery) { tab, position ->
            tab.customView = createTabView(position)
        }.also { it.attach() }
        updateTabState(binding.vpGallery.currentItem)
    }

    private fun createTabView(position: Int): ThemeTabIcon {
        val title = when (position) {
            MyGalleryPagerAdapter.PAGE_THEMES -> getString(R.string.themes)
            MyGalleryPagerAdapter.PAGE_WALLPAPERS -> getString(R.string.wallpapers)
            else -> ""
        }
        val iconRes = when (position) {
            MyGalleryPagerAdapter.PAGE_THEMES -> R.drawable.ic_tab_theme_mygallery_selector
            MyGalleryPagerAdapter.PAGE_WALLPAPERS -> R.drawable.ic_tab_wallpaper_mygallery_selector
            else -> 0
        }
        return ThemeTabIcon(requireContext()).apply {
            setName(title)
            setIcon(
                if (iconRes != 0) {
                    ContextCompat.getDrawable(requireContext(), iconRes)
                } else {
                    null
                }
            )
            setActiveBackground(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_tab_selected
                )
            )
            setInactiveBackground(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_tab_unselected
                )
            )
            setActiveTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            setInactiveTextColor(ContextCompat.getColor(requireContext(), R.color.text_med_em))
            inactivate()
        }
    }

    private fun updateTabState(selectedPosition: Int) {
        for (index in 0 until binding.tabCategory.tabCount) {
            val tabView = binding.tabCategory.getTabAt(index)?.customView as? ThemeTabIcon
            if (index == selectedPosition) {
                tabView?.activate()
            } else {
                tabView?.inactivate()
            }
        }
    }

    override fun release() {
        binding.vpGallery.unregisterOnPageChangeCallback(onPageChangeCallback)
        tabMediator?.detach()
        tabMediator = null
        binding.vpGallery.adapter = null
        super.release()
    }
}
