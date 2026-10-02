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

package thinklife.prism.wallpaper.widget.theme.presentation.search

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.core.extensions.hideKeyboard
import thinklife.prism.wallpaper.widget.theme.core.extensions.showKeyboard
import thinklife.prism.wallpaper.widget.theme.databinding.ActivitySearchBinding
import thinklife.prism.wallpaper.widget.theme.presentation.icons.adapter.IconAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.theme.adapter.ThemeAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.adapter.WallpaperAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.widget.adapter.WidgetAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.widget.dialog.AddWidgetsBottomSheetDialog
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.SetIconsActivity
import thinklife.prism.wallpaper.widget.theme.presentation.search.adapter.SearchChipAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.search.model.SearchMode
import thinklife.prism.wallpaper.widget.theme.presentation.search.model.SearchUiState
import thinklife.prism.wallpaper.widget.theme.presentation.search.viewmodel.SearchViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.theme.activities.PreviewThemeActivity
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.PreviewWallpaperActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding>() {

    override val adsScreenId = AdsScreen.SEARCH_VIEW

    override fun bindingView(): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(layoutInflater)
    }

    companion object {
        private const val EXTRA_MODE = "extra_mode"

        fun newIntent(context: Context, mode: SearchMode): Intent {
            return Intent(context, SearchActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode.name)
            }
        }
    }

    private val viewModel by viewModels<SearchViewModel>()
    private val themeAdapter = ThemeAdapter()
    private val wallpaperAdapter = WallpaperAdapter()
    private val iconAdapter = IconAdapter()
    private val widgetAdapter = WidgetAdapter()
    private val popularAdapter = SearchChipAdapter()
    private val searchMode by lazy {
        runCatching {
            SearchMode.valueOf(intent.getStringExtra(EXTRA_MODE).orEmpty())
        }.getOrDefault(SearchMode.THEMES)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.rcvThemes.adapter = when (searchMode) {
            SearchMode.THEMES -> themeAdapter
            SearchMode.WALLPAPERS -> wallpaperAdapter
            SearchMode.ICONS -> iconAdapter.also {
                binding.rcvThemes.layoutManager = LinearLayoutManager(this)
                val horizontalPadding = resources.getDimensionPixelSize(R.dimen._8sdp)
                binding.rcvThemes.updatePadding(
                    left = horizontalPadding,
                    right = horizontalPadding,
                )
            }
            SearchMode.WIDGETS -> widgetAdapter
        }
        binding.rcvPopular.adapter = popularAdapter
        binding.edSearch.setHint(
            when (searchMode) {
                SearchMode.THEMES -> R.string.search_themes
                SearchMode.WALLPAPERS -> R.string.search_wallpapers
                SearchMode.ICONS -> R.string.search_icons
                SearchMode.WIDGETS -> R.string.search_widgets
            }
        )
        binding.tvNewestTitle.setText(
            when (searchMode) {
                SearchMode.THEMES -> R.string.newest_themes
                SearchMode.WALLPAPERS -> R.string.newest_wallpapers
                SearchMode.ICONS -> R.string.newest_icons
                SearchMode.WIDGETS -> R.string.newest_widgets
            }
        )
        binding.edSearch.showKeyboard()
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        viewModel.load(searchMode)
    }



    override fun initListener() {
        super.initListener()
        binding.btnClose.setOnClickListener { onHandleBackPressed() }

        binding.edSearch.doAfterTextChanged { text ->
            viewModel.search(text?.toString().orEmpty())
        }
        binding.edSearch.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                view.hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.viewError.setOnRetryClickListener {
            triggerAds(AdsTrigger.SEARCH_CLICK_ERROR_RETRY) {
                runWhenResumed {
                    viewModel.load(searchMode, forceRefresh = true)
                }
            }
        }

        popularAdapter.onClickItem = { category ->
            triggerAds(AdsTrigger.SEARCH_CLICK_POPULAR_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    binding.edSearch.setText(category.name)
                    binding.edSearch.setSelection(binding.edSearch.text?.length ?: 0)
                }
            }
        }

        themeAdapter.onClickItem = { theme ->
            triggerAds(AdsTrigger.SEARCH_CLICK_THEME_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    startActivity(PreviewThemeActivity.newIntent(this, theme.id))
                }
            }
        }
        wallpaperAdapter.onClickItem = { wallpaper ->
            triggerAds(AdsTrigger.SEARCH_CLICK_WALLPAPER_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    startActivity(PreviewWallpaperActivity.newIntent(this, wallpaper))
                }
            }
        }
        iconAdapter.onClickItem = { pack ->
            triggerAds(AdsTrigger.SEARCH_CLICK_ICON_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    startActivity(SetIconsActivity.newIntent(this, pack.id, pack.name))
                }
            }
        }
        widgetAdapter.onClickItem = { widget ->
            triggerAds(AdsTrigger.SEARCH_CLICK_WIDGET_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    AddWidgetsBottomSheetDialog.newInstance(widget)
                        .show(supportFragmentManager, AddWidgetsBottomSheetDialog.TAG)
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.uiState.collectWithLifecycle(this) { state ->
            updateUiForState(state)
        }
    }

    private fun updateUiForState(state: SearchUiState) {
        binding.viewLoading.isVisible = state.isLoading
        binding.viewError.isVisible = state.message != null

        val canShowContent = !state.isLoading && state.message == null && !state.isEmpty
        val canShowPopular = canShowContent && !state.isSearching &&
                state.popularCategories.isNotEmpty()

        binding.viewEmpty.isVisible = state.isEmpty
        binding.rcvThemes.isVisible = canShowContent && state.hasResults

        binding.tvPopularTitle.isVisible = canShowPopular
        binding.rcvPopular.isVisible = canShowPopular
        binding.tvNewestTitle.isVisible = canShowContent && !state.isSearching

        popularAdapter.set(state.popularCategories)
        when (state.mode) {
            SearchMode.THEMES -> themeAdapter.set(state.themes)
            SearchMode.WALLPAPERS -> wallpaperAdapter.set(state.wallpapers)
            SearchMode.ICONS -> iconAdapter.set(state.iconPacks)
            SearchMode.WIDGETS -> widgetAdapter.set(state.widgets)
        }
        binding.tvEmpty.text = getString(
            R.string.no_results_found_for,
            state.query,
        )
    }
    override fun onHandleBackPressed() {
        triggerBackAds(AdsTrigger.SEARCH_CLICK_BACK) { finish() }
    }

}
