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

package thinklife.prism.wallpaper.widget.theme.presentation.theme

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.appbar.AppBarLayout
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeCategoryModel

import thinklife.prism.wallpaper.widget.theme.databinding.FragmentThemeBinding
import thinklife.prism.wallpaper.widget.theme.core.widget.ThemeTabIcon
import thinklife.prism.wallpaper.widget.theme.presentation.main.dialog.FilterPopupHelper
import thinklife.prism.wallpaper.widget.theme.presentation.theme.adapter.ThemeBannerAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.theme.adapter.ThemeCategoryPagerAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.theme.transformer.ThemeBannerPageTransformer
import thinklife.prism.wallpaper.widget.theme.presentation.theme.viewmodel.ThemeViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.search.SearchActivity
import thinklife.prism.wallpaper.widget.theme.presentation.search.model.SearchMode
import thinklife.prism.wallpaper.widget.theme.presentation.setting.SettingActivity
import thinklife.prism.wallpaper.widget.theme.presentation.theme.activities.DetailBannerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class ThemeFragment : BaseFragment<FragmentThemeBinding>() {

    override val adsScreenId = AdsScreen.HOME_VIEW

    override fun bindingView(): FragmentThemeBinding {
        return FragmentThemeBinding.inflate(layoutInflater)
    }

    companion object {
        private const val BANNER_AUTO_SCROLL_DELAY_MS = 3_000L
        private const val MIN_INFINITE_BANNER_COUNT = 3

        fun newInstance(): ThemeFragment {
            return ThemeFragment()
        }
    }

    private val filterHelper by lazy { FilterPopupHelper(requireContext(), layoutInflater) }
    private val viewModel by viewModels<ThemeViewModel>()
    private val bannerAdapter = ThemeBannerAdapter()
    private var tabMediator: TabLayoutMediator? = null

    private val appBarOffsetChangedListener =
        AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val scrollRange = appBarLayout.totalScrollRange
            val collapseProgress = if (scrollRange == 0) {
                0f
            } else {
                abs(verticalOffset).toFloat() / scrollRange
            }
            binding.bannerContainer.alpha = 1f - collapseProgress
        }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateTabState(position)
        }
    }

    private var lastSetupCategories: List<ThemeCategoryModel>? = null

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.toolbar.tvTitle.setText(R.string.themes)
        binding.appBar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
        binding.vpBanner.adapter = bannerAdapter
        setupBannerCarousel()
        binding.appBar.addOnOffsetChangedListener(appBarOffsetChangedListener)
        binding.vpTheme.registerOnPageChangeCallback(onPageChangeCallback)
        startBannerAutoScroll()
    }


    override fun initListener() {
        super.initListener()
        binding.toolbar.btnSetting.setOnClickListener {
            triggerAds(AdsTrigger.HOME_CLICK_SETTING) {
                runWhenResumed {
                    startActivity(SettingActivity.newIntent(requireContext()))
                }
            }
        }
        binding.toolbar.btnMyGallery.setOnClickListener {
            triggerAds(AdsTrigger.HOME_CLICK_MY_GALLERY) {
                runWhenResumed {
                    (activity as? MainActivity)?.showMyGallery()
                }
            }
        }

        binding.edSearch.setOnClickListener {
            triggerAds(AdsTrigger.HOME_CLICK_ED_SEARCH) {
                runWhenResumed {
                    startActivity(SearchActivity.newIntent(requireContext(), SearchMode.THEMES))
                }
            }
        }

        bannerAdapter.onClickItem = { category ->
            triggerAds(AdsTrigger.HOME_CLICK_BANNER_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    navigateTo(
                        DetailBannerActivity.newIntent(
                            context = requireContext(),
                            categoryId = category.id,
                            bannerUrl = category.banner.orEmpty(),
                        )
                    )
                }
            }
        }

        binding.viewError.setOnRetryClickListener {
            triggerAds(AdsTrigger.HOME_CLICK_ERROR_RETRY) {
                runWhenResumed {
                    viewModel.getCategories(forceRefresh = true)
                }
            }
        }

        binding.btnMoreCategory.setOnClickListener {
            triggerAds(AdsTrigger.HOME_CLICK_MORE_CATEGORY) {
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
                    val popupItems = categories.map { it.toFilterCategoryItem().copy(title = formatCategoryDisplayName(it.name)) }

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

        binding.btnRetry.setOnClickListener {
            triggerAds(AdsTrigger.HOME_CLICK_RETRY) {
                runWhenResumed {
                    viewModel.getCategories(forceRefresh = true)
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.categoriesResult.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Data -> {
                    setViewState(showContent = true, showLoading = false, showError = false)
                    setupBanners(state.value)
                    setupTabs(state.value)
                }
                is UiState.Error -> {
                    Log.d("hgahsdgad", "initObserver: ${state.message}")
                    setViewState(showContent = false, showLoading = false, showError = true)
                }
                UiState.Loading -> {
                    setViewState(showContent = false, showLoading = true, showError = false)
                }
                else -> Unit
            }
        }
    }

    private fun setViewState(showContent: Boolean, showLoading: Boolean, showError: Boolean) {
        binding.vpTheme.isVisible = showContent
        binding.viewLoading.isVisible = showLoading
        binding.viewLoadingBanner.isVisible = showLoading
        binding.viewError.isVisible = showError
        binding.viewErrorBanner.isVisible = showError
    }

    private fun setupTabs(categories: List<ThemeCategoryModel>) {
        if (categories == lastSetupCategories) return
        lastSetupCategories = categories
        val savedPosition = binding.vpTheme.currentItem
        tabMediator?.detach()
        binding.vpTheme.adapter = ThemeCategoryPagerAdapter(this, categories)
        tabMediator = TabLayoutMediator(binding.tabCategory, binding.vpTheme) { tab, position ->
            val item = categories[position]
            tab.customView = ThemeTabIcon(requireContext()).apply {
                setName(formatCategoryDisplayName(item.name))
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
                setInactiveTextColor(ContextCompat.getColor(requireContext(), R.color.prism_tab_inactive_text))
                inactivate()
            }
        }.also { it.attach() }

        if (savedPosition > 0 && savedPosition < categories.size) {
            binding.vpTheme.setCurrentItem(savedPosition, true)
        }
        updateTabState(binding.vpTheme.currentItem)
    }

    private fun formatCategoryDisplayName(rawName: String): String {
        return when (rawName.trim().lowercase()) {
            "hot" -> "Trending"
            "all" -> "Explore"
            "sport", "sports" -> "Sports"
            "cartoon", "cartoons" -> "Animation"
            "anime" -> "Anime"
            "aesthetic", "aesthetics" -> "Aesthetics"
            "cute" -> "Cute & Kawaii"
            "game", "games" -> "Gaming"
            "nature" -> "Nature"
            "cyber", "cyberpunk" -> "Cyber"
            "minimal", "minimalist" -> "Minimal"
            "neon" -> "Neon Glow"
            else -> rawName.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }

    private fun setupBanners(categories: List<ThemeCategoryModel>) {
        val bannerCategories = categories.filter { !it.banner.isNullOrBlank() }
        binding.vpBanner.isVisible = bannerCategories.isNotEmpty()
        val bannersChanged = bannerAdapter.submit(bannerCategories)
        if (bannerCategories.size >= MIN_INFINITE_BANNER_COUNT &&
            (bannersChanged || binding.vpBanner.currentItem == 0)
        ) {
            val startPosition = Int.MAX_VALUE / 2
            binding.vpBanner.setCurrentItem(
                startPosition - bannerAdapter.realPosition(startPosition),
                false,
            )
        }
    }

    private fun setupBannerCarousel() {
        binding.vpBanner.clipChildren = false
        binding.vpBanner.clipToPadding = false
        (binding.vpBanner.getChildAt(0) as? RecyclerView)?.apply {
            clipChildren = false
            clipToPadding = false
            overScrollMode = View.OVER_SCROLL_NEVER
            val horizontalPadding = resources.getDimensionPixelSize(R.dimen._48sdp)
            setPadding(horizontalPadding, paddingTop, horizontalPadding, paddingBottom)
        }
        binding.vpBanner.offscreenPageLimit = 3
        binding.vpBanner.setPageTransformer(ThemeBannerPageTransformer())
    }

    private fun startBannerAutoScroll() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    delay(BANNER_AUTO_SCROLL_DELAY_MS)
                    if (bannerAdapter.realCount > 1 && binding.vpBanner.isVisible) {
                        val nextItem =
                            if (binding.vpBanner.currentItem == binding.vpBanner.adapterItemCount() - 1) {
                                0
                            } else {
                                binding.vpBanner.currentItem + 1
                            }
                        binding.vpBanner.setCurrentItem(nextItem, true)
                    }
                }
            }
        }
    }

    private fun ViewPager2.adapterItemCount(): Int {
        return adapter?.itemCount ?: 0
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

    override fun onHandleBackPressed(): Boolean {
        if (filterHelper.isShowing) {
            filterHelper.dismiss()
            return true
        }
        return super.onHandleBackPressed()
    }

    override fun release() {
        filterHelper.dismiss()
        lastSetupCategories = null
        binding.appBar.removeOnOffsetChangedListener(appBarOffsetChangedListener)
        binding.vpTheme.unregisterOnPageChangeCallback(onPageChangeCallback)
        tabMediator?.detach()
        tabMediator = null
        binding.vpTheme.adapter = null
        super.release()
    }
}
