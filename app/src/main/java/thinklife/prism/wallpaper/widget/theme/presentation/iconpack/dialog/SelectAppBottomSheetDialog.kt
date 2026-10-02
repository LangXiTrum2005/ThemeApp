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

package thinklife.prism.wallpaper.widget.theme.presentation.iconpack.dialog

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import thinklife.prism.wallpaper.widget.theme.core.base.BaseBottomSheetDialog
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.data.model.AppInfo
import thinklife.prism.wallpaper.widget.theme.databinding.BottomSheetSelectAppBinding
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.adapter.SelectAppAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.viewmodel.SelectAppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectAppBottomSheetDialog : BaseBottomSheetDialog<BottomSheetSelectAppBinding>() {

    override val adsScreenId = AdsScreen.SELECT_APP_VIEW


    override fun bindingView() = BottomSheetSelectAppBinding.inflate(layoutInflater)

    companion object {
        private const val TAG = "SelectAppBottomSheetDialog"

        fun newInstance() = SelectAppBottomSheetDialog()
    }

    override val hideNavigationBar: Boolean
        get() = true

    private val viewModel by viewModels<SelectAppViewModel>()
    private val adapter = SelectAppAdapter()

    private var listener: OnAppSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnAppSelectedListener ?: parentFragment as? OnAppSelectedListener
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        binding.rcvApps.adapter = adapter
    }

    override fun initListener() {
        binding.btnClose.setOnClickListener {
            triggerAds(AdsTrigger.SELECT_APP_CLICK_CLOSE) {
                runWhenResumed { dismiss() }
            }
        }

        adapter.onClickItem = { app ->
            triggerAds(AdsTrigger.SELECT_APP_CLICK_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    listener?.onAppSelected(app)
                    dismiss()
                }
            }
        }
    }

    override fun initObserver() {
        viewModel.apps.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Loading -> binding.rcvApps.alpha = 0.4f
                is UiState.Data -> {
                    binding.rcvApps.alpha = 1f
                    adapter.set(state.value)
                }

                else -> binding.rcvApps.alpha = 1f
            }
        }
    }

    interface OnAppSelectedListener {
        fun onAppSelected(appInfo: AppInfo)
    }

}
