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

package thinklife.prism.wallpaper.widget.theme.presentation.widget

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetCategoryModel
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentWidgetsBinding
import thinklife.prism.wallpaper.widget.theme.core.widget.ThemeTabIcon
import thinklife.prism.wallpaper.widget.theme.presentation.main.dialog.FilterPopupHelper
import thinklife.prism.wallpaper.widget.theme.presentation.widget.adapter.WidgetCategoryPagerAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.widget.viewmodel.WidgetViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.search.SearchActivity
import thinklife.prism.wallpaper.widget.theme.presentation.search.model.SearchMode
import thinklife.prism.wallpaper.widget.theme.presentation.setting.SettingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetsFragment : BaseFragment<FragmentWidgetsBinding>() {

    override fun bindingView(): FragmentWidgetsBinding =
        FragmentWidgetsBinding.inflate(layoutInflater)

    companion object {
        fun newInstance() = WidgetsFragment()
    }

    private val filterHelper by lazy { FilterPopupHelper(requireContext(), layoutInflater) }
    private val viewModel by viewModels<WidgetViewModel>()

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            for (index in 0 until binding.tabCategory.tabCount) {
                val tabView = binding.tabCategory.getTabAt(index)?.customView as? ThemeTabIcon
                if (index == position) tabView?.activate() else tabView?.inactivate()
            }
        }
    }

    private var categories: List<WidgetCategoryModel> = emptyList()

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.toolbar.tvTitle.setText(R.string.widgets)
        binding.edSearch.setText(R.string.search_widgets)
        binding.vpTheme.registerOnPageChangeCallback(onPageChangeCallback)
    }


    override fun initListener() {
        super.initListener()
        binding.toolbar.btnSetting.setOnClickListener {
            startActivity(SettingActivity.newIntent(requireContext()))
        }
        binding.toolbar.btnMyGallery.setOnClickListener {
            (activity as? MainActivity)?.showMyGallery()
        }
        binding.edSearch.setOnClickListener {
            startActivity(SearchActivity.newIntent(requireContext(), SearchMode.WIDGETS))
        }
        binding.viewError.setOnRetryClickListener {
            viewModel.getCategories(forceRefresh = true)
        }
        binding.btnMoreCategory.setOnClickListener { anchor ->
            val categories = (viewModel.categoriesState.value as? UiState.Data)?.value
                ?: return@setOnClickListener
            if (categories.isEmpty()) return@setOnClickListener

            val selectedPosition = binding.tabCategory.selectedTabPosition.coerceAtLeast(0)
            val selectedCategory = categories.getOrElse(selectedPosition) { categories.first() }
            filterHelper.show(
                activity = activity ?: return@setOnClickListener,
                anchorView = anchor,
                items = categories.map { it.toFilterCategoryItem() },
                selectedItemId = selectedCategory.id,
            ) { selectedItem ->
                val position = categories.indexOfFirst { it.id == selectedItem.id }
                if (position >= 0) binding.vpTheme.currentItem = position
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.categoriesState.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Data -> {
                    binding.viewError.isVisible = false
                    binding.vpTheme.isVisible = true
                    binding.viewLoading.isVisible = false
                    val savedPosition = binding.vpTheme.currentItem
                    setupTabs(state.value)
                    if (savedPosition > 0) {
                        binding.vpTheme.setCurrentItem(savedPosition, false)
                    }
                }

                is UiState.Error -> {
                    binding.viewError.isVisible = true
                    binding.vpTheme.isVisible = false
                    binding.viewLoading.isVisible = false
                }

                UiState.Loading -> {
                    binding.viewError.isVisible = false
                    binding.viewLoading.isVisible = true
                }

                UiState.Idle -> Unit
            }
        }
    }

    private fun setupTabs(categories: List<WidgetCategoryModel>) {
        this.categories = categories
        binding.vpTheme.adapter = WidgetCategoryPagerAdapter(this, categories)
        TabLayoutMediator(binding.tabCategory, binding.vpTheme) { tab, position ->
            val category = categories[position]
            tab.customView = ThemeTabIcon(requireContext()).apply {
                setName(category.name)
                setIcon(category.icon)
                setActiveBackground(
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_selected)
                )
                setInactiveBackground(
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_unselected)
                )
                setActiveTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                setInactiveTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_med_em)
                )
                inactivate()
            }
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

    override fun release() {
        categories = emptyList()
        binding.vpTheme.unregisterOnPageChangeCallback(onPageChangeCallback)
        super.release()
    }
}
