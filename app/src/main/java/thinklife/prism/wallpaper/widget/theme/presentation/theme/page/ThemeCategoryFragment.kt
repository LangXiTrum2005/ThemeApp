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

package thinklife.prism.wallpaper.widget.theme.presentation.theme.page

import thinklife.prism.wallpaper.widget.theme.ads.AdsSlot
import thinklife.prism.wallpaper.widget.theme.ads.NativeFeedAdapter
import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentThemeCategoryBinding
import thinklife.prism.wallpaper.widget.theme.presentation.theme.adapter.ThemeAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.theme.viewmodel.ThemePageViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.theme.activities.PreviewThemeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeCategoryFragment : BaseFragment<FragmentThemeCategoryBinding>() {

    override val adsScreenId = AdsScreen.THEME_CATEGORY_VIEW


    override fun bindingView(): FragmentThemeCategoryBinding {
        return FragmentThemeCategoryBinding.inflate(layoutInflater)
    }

    private val categoryId: String by lazy {
        arguments?.getString(ARG_CATEGORY_ID).orEmpty()
    }

    private val themeAdapter = ThemeAdapter()
    private val viewModel by viewModels<ThemePageViewModel>()

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.rcvTheme.adapter = NativeFeedAdapter(themeAdapter, adsScreenId, AdsSlot.THEME_CATEGORY_SLOT_FEED, viewLifecycleOwner)
        viewModel.getThemesByCategory(categoryId)
    }

    override fun initListener() {
        super.initListener()
        themeAdapter.onClickItem = { theme ->
            triggerAds(AdsTrigger.THEME_CATEGORY_CLICK_THEME_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    navigateTo(PreviewThemeActivity.newIntent(requireContext(), theme.id))
                }
            }
        }

        binding.viewError.setOnRetryClickListener {
            triggerAds(AdsTrigger.THEME_CATEGORY_CLICK_ERROR_RETRY) {
                runWhenResumed {
                    viewModel.getThemesByCategory(categoryId, forceRefresh = true)
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.themesState.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Error -> {
                    setViewState(showContent = false, showLoading = false, showError = true)
                }

                UiState.Loading -> {
                    setViewState(showContent = false, showLoading = true, showError = false)
                }

                is UiState.Data -> {
                    setViewState(showContent = true, showLoading = false, showError = false)
                    themeAdapter.set(state.value)
                    binding.rcvTheme.scheduleLayoutAnimation()
                }

                else -> Unit
            }
        }
    }

    private fun setViewState(showContent: Boolean, showLoading: Boolean, showError: Boolean) {
        binding.rcvTheme.isVisible = showContent
        binding.viewLoading.isVisible = showLoading
        binding.viewError.isVisible = showError
    }


    companion object {
        private const val ARG_CATEGORY_ID = "category_id"

        fun newInstance(categoryId: String): ThemeCategoryFragment {
            return ThemeCategoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_ID, categoryId)
                }
            }
        }
    }
}
