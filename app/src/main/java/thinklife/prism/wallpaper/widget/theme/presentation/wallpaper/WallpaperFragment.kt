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

package thinklife.prism.wallpaper.widget.theme.presentation.wallpaper

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.data.model.WallpaperCategoryModel
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentWallpaperBinding
import thinklife.prism.wallpaper.widget.theme.core.widget.ThemeTabIcon
import thinklife.prism.wallpaper.widget.theme.presentation.main.dialog.FilterPopupHelper
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.adapter.CategoryPagerAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.viewmodel.WallpaperViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.search.SearchActivity
import thinklife.prism.wallpaper.widget.theme.presentation.search.model.SearchMode
import thinklife.prism.wallpaper.widget.theme.presentation.setting.SettingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WallpaperFragment : BaseFragment<FragmentWallpaperBinding>() {

    override val adsScreenId = AdsScreen.TAB2_VIEW

    override fun bindingView(): FragmentWallpaperBinding {
        return FragmentWallpaperBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance(): WallpaperFragment {
            return WallpaperFragment()
        }
    }

    private val filterHelper by lazy { FilterPopupHelper(requireContext(), layoutInflater) }

    private val viewModel by viewModels<WallpaperViewModel>()

    private val onPageChangeCallback: ViewPager2.OnPageChangeCallback = object :
        ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            // Iterate through tabs and set their state
            for (i in 0 until binding.tabCategory.tabCount) {
                val tab = binding.tabCategory.getTabAt(i)
                val customView = tab?.customView as? ThemeTabIcon
                if (i == position) {
                    // Activate the tab at the current position
                    customView?.activate()
                } else {
                    // Inactivate all other tabs
                    customView?.inactivate()
                }
            }
        }
    }

    private var categories: List<WallpaperCategoryModel> = emptyList()

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.wallpapers)
        binding.vpTheme.registerOnPageChangeCallback(onPageChangeCallback)
    }



    private fun setupTabs(
        categories: List<WallpaperCategoryModel>
    ) {
        this.categories = categories
        binding.vpTheme.adapter = CategoryPagerAdapter(this, categories)
        TabLayoutMediator(
            binding.tabCategory,
            binding.vpTheme
        ) { tab, position ->
            val item = categories[position]
            val tabView = ThemeTabIcon(requireContext()).apply {
                setName(item.name)
                setIcon(item.icon)
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
                setActiveTextColor(ContextCompat.getColor(requireContext(), R.color.prism_tab_active_text))
                setInactiveTextColor(ContextCompat.getColor(requireContext(), R.color.text_med_em))
                inactivate()
            }
            tab.customView = tabView
        }.attach()

        binding.tabCategory.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    (tab.customView as? ThemeTabIcon)?.activate()
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    (tab.customView as? ThemeTabIcon)?.inactivate()
                }

                override fun onTabReselected(tab: TabLayout.Tab) = Unit
            }
        )
    }


    override fun initListener() {
        super.initListener()
        binding.toolbar.apply {
            btnSetting.setOnClickListener {
            triggerAds(AdsTrigger.TAB2_CLICK_SETTING) {
                runWhenResumed {
                        startActivity(SettingActivity.newIntent(requireContext()))
                }
            }
        }
            btnMyGallery.setOnClickListener {
            triggerAds(AdsTrigger.TAB2_CLICK_MY_GALLERY) {
                runWhenResumed {
                        (activity as? MainActivity)?.showMyGallery()
                }
            }
        }

        }

        binding.edSearch.setOnClickListener {
            triggerAds(AdsTrigger.TAB2_CLICK_ED_SEARCH) {
                runWhenResumed {
                    startActivity(SearchActivity.newIntent(requireContext(), SearchMode.WALLPAPERS))
                }
            }
        }

        binding.viewError.setOnRetryClickListener {
            triggerAds(AdsTrigger.TAB2_CLICK_ERROR_RETRY) {
                runWhenResumed {
                    viewModel.getCategories()
                }
            }
        }

        binding.btnMoreCategory.setOnClickListener {
            triggerAds(AdsTrigger.TAB2_CLICK_MORE_CATEGORY) {
                runWhenResumed {
                    if (filterHelper.isShowing) {
                        filterHelper.dismiss()
                        return@runWhenResumed
                    }
                    val categories = (viewModel.categoriesResult.value as? UiState.Data)?.value
                        ?: return@runWhenResumed
                    if (categories.isEmpty()) return@runWhenResumed

                    val selectedPos = binding.tabCategory.selectedTabPosition.coerceAtLeast(0)
                    val selectedCategory = categories.getOrElse(selectedPos) { categories.first() }
                    val popupItems = categories.map { it.toFilterCategoryItem() }

                    filterHelper.show(
                        activity = activity ?: return@runWhenResumed,
                        anchorView = it,
                        items = popupItems,
                        selectedItemId = selectedCategory.id,
                    ) { selectedItem ->
                        val pos = categories.indexOfFirst { category -> category.id == selectedItem.id }
                        if (pos >= 0) binding.vpTheme.currentItem = pos
                    }
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.categoriesResult.collectWithLifecycle(this) {
            when (it) {
                is UiState.Data -> {
                    Log.d("Log_tabbb", "Data: ${it}")
                    binding.viewError.isVisible = false
                    binding.vpTheme.isVisible = true
                    binding.viewLoading.isVisible = false
                    val savedPosition = binding.vpTheme.currentItem
                    setupTabs(it.value)
                    if (savedPosition > 0) binding.vpTheme.setCurrentItem(savedPosition, false)
                }

                is UiState.Error -> {
                    Log.d("Log_tabbb", "initObserver: ${it.message}")
                    binding.viewError.isVisible = true
                    binding.vpTheme.isVisible = false
                    binding.viewLoading.isVisible = false
                }

                UiState.Loading -> {
                    binding.viewError.isVisible = false
                    binding.viewLoading.isVisible = true
                }

                else -> {}
            }
        }
    }

    override fun onHandleBackPressed(): Boolean {
        if (filterHelper.isShowing) {
            filterHelper.dismiss()
            return true
        }
        return super.onHandleBackPressed()
    }

    override fun release() {
        filterHelper.dismiss()
        categories = emptyList()
        binding.vpTheme.unregisterOnPageChangeCallback(onPageChangeCallback)
        super.release()
    }
}
