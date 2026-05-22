package com.wavez.controllerwidget.ui.home.fragment.theme

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.example.m65_gamemodefps.core.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.data.model.theme.ThemeCategoryModel
import com.wavez.controllerwidget.databinding.FragmentThemeBinding
import com.wavez.controllerwidget.ui.customview.ThemeTabIcon
import com.wavez.controllerwidget.ui.home.dialog.FilterPopupHelper
import com.wavez.controllerwidget.ui.home.fragment.theme.adapter.ThemeBannerAdapter
import com.wavez.controllerwidget.ui.home.fragment.theme.adapter.ThemeCategoryPagerAdapter
import com.wavez.controllerwidget.ui.home.fragment.theme.transformer.ThemeBannerPageTransformer
import com.wavez.controllerwidget.ui.home.fragment.theme.viewmodel.ThemeViewModel
import com.wavez.controllerwidget.ui.search.SearchActivity
import com.wavez.controllerwidget.ui.search.model.SearchMode
import com.wavez.controllerwidget.ui.setting.SettingActivity
import com.wavez.controllerwidget.ui.theme.activities.DetailBannerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ThemeFragment : BaseFragment<FragmentThemeBinding>() {
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

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateTabState(position)
        }
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.toolbar.tvTitle.setText(R.string.themes)
        binding.vpBanner.adapter = bannerAdapter
        setupBannerCarousel()
        binding.vpTheme.registerOnPageChangeCallback(onPageChangeCallback)
        startBannerAutoScroll()
    }

    override fun initListener() {
        super.initListener()
        binding.toolbar.btnSetting.setOnClickListener {
            startActivity(SettingActivity.newIntent(requireContext()))
        }

        binding.edSearch.setOnClickListener {
            startActivity(SearchActivity.newIntent(requireContext(), SearchMode.THEMES))
        }

        bannerAdapter.onClickItem = { category ->
            startActivity(
                DetailBannerActivity.newIntent(
                    context = requireContext(),
                    categoryId = category.id,
                    categoryName = category.name,
                    bannerUrl = category.banner.orEmpty(),
                )
            )
        }

        binding.viewError.setOnRetryClickListener {
            viewModel.getCategories(forceRefresh = true)
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
        viewModel.categoriesResult.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Data -> {
                    binding.viewError.isVisible = false
                    binding.viewLoading.isVisible = false
                    binding.vpTheme.isVisible = true
                    setupBanners(state.value)
                    setupTabs(state.value)
                }

                is UiState.Error -> {
                    Log.d("hgahsdgad", "initObserver: ${state.message}")
                    binding.viewError.isVisible = true
                    binding.viewLoading.isVisible = false
                    binding.vpTheme.isVisible = false
                }

                UiState.Loading -> {
                    binding.viewError.isVisible = false
                    binding.viewLoading.isVisible = true
                }

                else -> Unit
            }
        }
    }

    private fun setupTabs(categories: List<ThemeCategoryModel>) {
        val savedPosition = binding.vpTheme.currentItem
        tabMediator?.detach()
        binding.vpTheme.adapter = ThemeCategoryPagerAdapter(this, categories)
        tabMediator = TabLayoutMediator(binding.tabCategory, binding.vpTheme) { tab, position ->
            val item = categories[position]
            tab.customView = ThemeTabIcon(requireContext()).apply {
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
        }.also { it.attach() }

        if (savedPosition > 0 && savedPosition < categories.size) {
            binding.vpTheme.setCurrentItem(savedPosition, false)
        }
        updateTabState(binding.vpTheme.currentItem)
    }

    private fun setupBanners(categories: List<ThemeCategoryModel>) {
        val bannerCategories = categories.filter { !it.banner.isNullOrBlank() }
        binding.vpBanner.isVisible = bannerCategories.isNotEmpty()
        bannerAdapter.submit(bannerCategories)
        if (bannerCategories.size >= MIN_INFINITE_BANNER_COUNT) {
            val startPosition = Int.MAX_VALUE / 2
            binding.vpBanner.setCurrentItem(
                startPosition - bannerAdapter.realPosition(startPosition),
                false,
            )
        }
    }

    private fun setupBannerCarousel() {
        binding.root.clipChildren = false
        binding.root.clipToPadding = false
        binding.vpBanner.clipChildren = false
        binding.vpBanner.clipToPadding = false
        binding.vpBanner.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER
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

    override fun release() {
        binding.vpTheme.unregisterOnPageChangeCallback(onPageChangeCallback)
        tabMediator?.detach()
        tabMediator = null
        binding.vpTheme.adapter = null
        super.release()
    }
}
