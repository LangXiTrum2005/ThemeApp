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

package thinklife.prism.wallpaper.widget.theme.presentation.widget.page

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
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentWallpaperCategoryBinding
import thinklife.prism.wallpaper.widget.theme.presentation.widget.adapter.WidgetAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.widget.dialog.AddWidgetsBottomSheetDialog
import thinklife.prism.wallpaper.widget.theme.presentation.widget.viewmodel.WidgetPageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetCategoryFragment : BaseFragment<FragmentWallpaperCategoryBinding>() {

    override val adsScreenId = AdsScreen.WIDGET_CATEGORY_VIEW


    override fun bindingView(): FragmentWallpaperCategoryBinding =
        FragmentWallpaperCategoryBinding.inflate(layoutInflater)

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"

        fun newInstance(categoryId: String) = WidgetCategoryFragment().apply {
            arguments = Bundle().apply { putString(ARG_CATEGORY_ID, categoryId) }
        }
    }

    private val categoryId by lazy { arguments?.getString(ARG_CATEGORY_ID).orEmpty() }
    private val widgetAdapter = WidgetAdapter()
    private val viewModel by viewModels<WidgetPageViewModel>()

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.rcvWallpaper.adapter = NativeFeedAdapter(widgetAdapter, adsScreenId, AdsSlot.WIDGET_CATEGORY_SLOT_FEED, viewLifecycleOwner)
        viewModel.getWidgetsByCategory(categoryId)
    }

    override fun initListener() {
        super.initListener()
        widgetAdapter.onClickItem = { widget ->
            triggerAds(AdsTrigger.WIDGET_CATEGORY_CLICK_WIDGET_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    AddWidgetsBottomSheetDialog.newInstance(widget)
                        .show(childFragmentManager, AddWidgetsBottomSheetDialog.TAG)
                }
            }
        }
        binding.viewError.setOnRetryClickListener {
            triggerAds(AdsTrigger.WIDGET_CATEGORY_CLICK_ERROR_RETRY) {
                runWhenResumed {
                    viewModel.getWidgetsByCategory(categoryId, forceRefresh = true)
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.widgetsState.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Data -> {
                    setViewState(showContent = true, showLoading = false, showError = false)
                    widgetAdapter.set(state.value)
                }

                is UiState.Error -> {
                    setViewState(showContent = false, showLoading = false, showError = true)
                }

                UiState.Loading -> {
                    setViewState(showContent = false, showLoading = true, showError = false)
                }

                UiState.Idle -> Unit
            }
        }
    }

    private fun setViewState(showContent: Boolean, showLoading: Boolean, showError: Boolean) {
        binding.rcvWallpaper.isVisible = showContent
        binding.viewLoading.isVisible = showLoading
        binding.viewError.isVisible = showError
    }
    override fun onPrepareAdsSlots() {
        binding.adsSlotTop.tag = thinklife.prism.wallpaper.widget.theme.ads.AdsSlot.tag(thinklife.prism.wallpaper.widget.theme.ads.AdsSlot.WIDGET_CATEGORY_SLOT_TOP)
        binding.adsSlotBottom.tag = thinklife.prism.wallpaper.widget.theme.ads.AdsSlot.tag(thinklife.prism.wallpaper.widget.theme.ads.AdsSlot.WIDGET_CATEGORY_SLOT_BOTTOM)
    }

}
