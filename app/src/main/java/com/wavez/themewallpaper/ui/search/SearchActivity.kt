package com.wavez.themewallpaper.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.ads.AdsId
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.core.extensions.hideKeyboard
import com.wavez.themewallpaper.core.extensions.showKeyboard
import com.wavez.themewallpaper.databinding.ActivitySearchBinding
import com.wavez.themewallpaper.ui.home.fragment.icons.adapter.IconAdapter
import com.wavez.themewallpaper.ui.home.fragment.theme.adapter.ThemeAdapter
import com.wavez.themewallpaper.ui.home.fragment.wallpaper.adapter.WallpaperAdapter
import com.wavez.themewallpaper.ui.iconpack.SetIconsActivity
import com.wavez.themewallpaper.ui.search.adapter.SearchChipAdapter
import com.wavez.themewallpaper.ui.search.model.SearchMode
import com.wavez.themewallpaper.ui.search.model.SearchUiState
import com.wavez.themewallpaper.ui.search.viewmodel.SearchViewModel
import com.wavez.themewallpaper.ui.theme.activities.PreviewThemeActivity
import com.wavez.themewallpaper.ui.wallpaper.PreviewWallpaperActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding>() {
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
        }
        binding.rcvPopular.adapter = popularAdapter
        binding.edSearch.setHint(
            when (searchMode) {
                SearchMode.THEMES -> R.string.search_themes
                SearchMode.WALLPAPERS -> R.string.search_wallpapers
                SearchMode.ICONS -> R.string.search_icons
            }
        )
        binding.tvNewestTitle.setText(
            when (searchMode) {
                SearchMode.THEMES -> R.string.newest_themes
                SearchMode.WALLPAPERS -> R.string.newest_wallpapers
                SearchMode.ICONS -> R.string.newest_icons
            }
        )
        binding.edSearch.showKeyboard()
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        viewModel.load(searchMode)
        loadBanner()
    }

    private fun loadBanner() {
        binding.bannerAds.registerBanner(
            this, adId = AdsId.getBannerInAppAds(),
            shouldLoad = sharedPref.shouldShowBannerInApp ,
        )
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
            viewModel.load(searchMode, forceRefresh = true)
        }

        popularAdapter.onClickItem = { category ->
            binding.edSearch.setText(category.name)
            binding.edSearch.setSelection(binding.edSearch.text?.length ?: 0)
        }

        themeAdapter.onClickItem = { theme ->
            startActivity(PreviewThemeActivity.newIntent(this, theme.id))
        }
        wallpaperAdapter.onClickItem = { wallpaper ->
            startActivity(PreviewWallpaperActivity.newIntent(this, wallpaper))
        }
        iconAdapter.onClickItem = { pack ->
            startActivity(SetIconsActivity.newIntent(this, pack.id, pack.name))
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
        }
        binding.tvEmpty.text = getString(
            R.string.no_results_found_for,
            state.query,
        )
    }

    override fun onHandleBackPressed() {
        executeWithAds {
            super.onHandleBackPressed()
        }
    }
}
