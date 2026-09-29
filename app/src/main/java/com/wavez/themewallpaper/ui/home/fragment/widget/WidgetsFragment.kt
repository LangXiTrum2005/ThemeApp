package com.wavez.themewallpaper.ui.home.fragment.widget

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.base.BaseFragment
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.data.model.widget.WidgetCategoryModel
import com.wavez.themewallpaper.databinding.FragmentWidgetsBinding
import com.wavez.themewallpaper.ui.customview.ThemeTabIcon
import com.wavez.themewallpaper.ui.home.dialog.FilterPopupHelper
import com.wavez.themewallpaper.ui.home.fragment.widget.adapter.WidgetCategoryPagerAdapter
import com.wavez.themewallpaper.ui.home.fragment.widget.viewmodel.WidgetViewModel
import com.wavez.themewallpaper.ui.home.MainActivity
import com.wavez.themewallpaper.ui.search.SearchActivity
import com.wavez.themewallpaper.ui.search.model.SearchMode
import com.wavez.themewallpaper.ui.setting.SettingActivity
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
        binding.vpTheme.unregisterOnPageChangeCallback(onPageChangeCallback)
        super.release()
    }
}
