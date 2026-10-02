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

package thinklife.prism.wallpaper.widget.theme.presentation.mygallery.page

import thinklife.prism.wallpaper.widget.theme.ads.AdsSlot
import thinklife.prism.wallpaper.widget.theme.ads.NativeFeedAdapter
import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetsModel
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentMyGalleryPageBinding
import thinklife.prism.wallpaper.widget.theme.presentation.mygallery.adapter.MyGalleryWidgetAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.mygallery.viewmodel.MyGalleryViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.widget.dialog.AddWidgetsBottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyGalleryWidgetsFragment : BaseFragment<FragmentMyGalleryPageBinding>() {

    override val adsScreenId = AdsScreen.GALLERY_WIDGETS_VIEW


    override fun bindingView(): FragmentMyGalleryPageBinding {
        return FragmentMyGalleryPageBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance(): MyGalleryWidgetsFragment = MyGalleryWidgetsFragment()
    }

    private val viewModel by activityViewModels<MyGalleryViewModel>()
    private val adapter = MyGalleryWidgetAdapter()

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.rcvGallery.adapter = NativeFeedAdapter(adapter, adsScreenId, AdsSlot.GALLERY_WIDGETS_SLOT_FEED, viewLifecycleOwner)
    }

    override fun initListener() {
        super.initListener()
        adapter.onClickItem = { widget ->
            triggerAds(AdsTrigger.GALLERY_WIDGETS_CLICK_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    AddWidgetsBottomSheetDialog.newInstance(widget)
                        .show(childFragmentManager, AddWidgetsBottomSheetDialog.TAG)
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.widgetsState.collectWithLifecycle(this) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: UiState<List<WidgetsModel>>) {
        when (state) {
            is UiState.Data -> {
                adapter.set(state.value)
                binding.viewLoading.isVisible = false
                binding.rcvGallery.isVisible = state.value.isNotEmpty()
                binding.tvEmpty.isVisible = state.value.isEmpty()
            }

            is UiState.Loading -> {
                binding.viewLoading.isVisible = true
                binding.rcvGallery.isVisible = false
                binding.tvEmpty.isVisible = false
            }

            is UiState.Error -> {
                adapter.set(emptyList())
                binding.viewLoading.isVisible = false
                binding.rcvGallery.isVisible = false
                binding.tvEmpty.isVisible = true
            }

            else -> Unit
        }
    }
    override fun onPrepareAdsSlots() {
        binding.adsSlotTop.tag = thinklife.prism.wallpaper.widget.theme.ads.AdsSlot.tag(thinklife.prism.wallpaper.widget.theme.ads.AdsSlot.GALLERY_WIDGETS_SLOT_TOP)
        binding.adsSlotBottom.tag = thinklife.prism.wallpaper.widget.theme.ads.AdsSlot.tag(thinklife.prism.wallpaper.widget.theme.ads.AdsSlot.GALLERY_WIDGETS_SLOT_BOTTOM)
    }

}
