package com.wavez.themewallpaper.ui.home.fragment.wallpaper

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.wavez.themewallpaper.core.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.data.model.WallpaperCategoryModel
import com.wavez.themewallpaper.databinding.FragmentWallpaperBinding
import com.wavez.themewallpaper.ui.customview.ThemeTabIcon
import com.wavez.themewallpaper.ui.home.dialog.FilterPopupHelper
import com.wavez.themewallpaper.ui.home.fragment.wallpaper.adapter.CategoryPagerAdapter
import com.wavez.themewallpaper.ui.home.fragment.wallpaper.viewmodel.WallpaperViewModel
import com.wavez.themewallpaper.ui.premium.PremiumActivity
import com.wavez.themewallpaper.ui.search.SearchActivity
import com.wavez.themewallpaper.ui.search.model.SearchMode
import com.wavez.themewallpaper.ui.setting.SettingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WallpaperFragment : BaseFragment<FragmentWallpaperBinding>() {
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

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.wallpapers)
        binding.vpTheme.registerOnPageChangeCallback(onPageChangeCallback)
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.btnPremium.isVisible = !sharedPref.isVip
    }


    private fun setupTabs(
        categories: List<WallpaperCategoryModel>
    ) {
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
                setActiveTextColor(ContextCompat.getColor(requireContext(), R.color.white))
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
                startActivity(SettingActivity.newIntent(requireContext()))
            }

            btnPremium.setOnClickListener {
                startActivity(PremiumActivity.newIntent(requireContext()))
            }
        }

        binding.edSearch.setOnClickListener {
            startActivity(SearchActivity.newIntent(requireContext(), SearchMode.WALLPAPERS))
        }

        binding.viewError.setOnRetryClickListener {
            viewModel.getCategories()
        }

        binding.btnMoreCategory.setOnClickListener {
            val categories = (viewModel.categoriesResult.value as? UiState.Data)?.value
                ?: return@setOnClickListener
            if (categories.isEmpty()) return@setOnClickListener

            val selectedPos = binding.tabCategory.selectedTabPosition.coerceAtLeast(0)
            val selectedCategory = categories.getOrElse(selectedPos) { categories.first() }
            val popupItems = categories.map { it.toFilterCategoryItem() }

            filterHelper.show(
                activity = activity ?: return@setOnClickListener,
                anchorView = it,
                items = popupItems,
                selectedItemId = selectedCategory.id,
            ) { selectedItem ->
                val pos = categories.indexOfFirst { category -> category.id == selectedItem.id }
                if (pos >= 0) binding.vpTheme.currentItem = pos
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

    override fun release() {
        binding.vpTheme.unregisterOnPageChangeCallback(onPageChangeCallback)
        super.release()
    }
}
