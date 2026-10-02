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

package thinklife.prism.wallpaper.widget.theme.presentation.icons

import thinklife.prism.wallpaper.widget.theme.ads.AdsSlot
import thinklife.prism.wallpaper.widget.theme.ads.NativeFeedAdapter
import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentIconBinding
import thinklife.prism.wallpaper.widget.theme.presentation.icons.adapter.IconAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.icons.viewmodel.IconsViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.SetIconsActivity
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.search.SearchActivity
import thinklife.prism.wallpaper.widget.theme.presentation.search.model.SearchMode
import thinklife.prism.wallpaper.widget.theme.presentation.setting.SettingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IconFragment : BaseFragment<FragmentIconBinding>() {

    override val adsScreenId = AdsScreen.ICONS_VIEW

    override fun bindingView(): FragmentIconBinding {
        return FragmentIconBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance(): IconFragment {
            return IconFragment()
        }
    }

    private val viewModel by viewModels<IconsViewModel>()

    private val iconAdapter = IconAdapter()

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        viewModel.getIcons()
        binding.toolbar.tvTitle.text = getString(R.string.icons)
        binding.rcvIcons.adapter = NativeFeedAdapter(iconAdapter, adsScreenId, AdsSlot.ICONS_SLOT_FEED, viewLifecycleOwner)
    }



    override fun initListener() {
        super.initListener()
        binding.toolbar.btnSetting.setOnClickListener {
            triggerAds(AdsTrigger.ICONS_CLICK_SETTING) {
                runWhenResumed {
                    startActivity(SettingActivity.newIntent(requireContext()))
                }
            }
        }
        binding.toolbar.btnMyGallery.setOnClickListener {
            triggerAds(AdsTrigger.ICONS_CLICK_MY_GALLERY) {
                runWhenResumed {
                    (activity as? MainActivity)?.showMyGallery()
                }
            }
        }

        binding.edSearch.setOnClickListener {
            triggerAds(AdsTrigger.ICONS_CLICK_ED_SEARCH) {
                runWhenResumed {
                    startActivity(SearchActivity.newIntent(requireContext(), SearchMode.ICONS))
                }
            }
        }


        binding.viewError.setOnRetryClickListener {
            triggerAds(AdsTrigger.ICONS_CLICK_ERROR_RETRY) {
                runWhenResumed {
                    viewModel.getIcons()
                }
            }
        }

        iconAdapter.onClickItem = { pack ->
            triggerAds(AdsTrigger.ICONS_CLICK_ICON_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    startActivity(
                        SetIconsActivity.newIntent(
                            requireContext(), pack.id, pack.name
                        )
                    )
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.iconResult.collectWithLifecycle(this) {
            when (it) {
                is UiState.Data -> {
                    Log.d("Log_tabbb", "Data: ${it}")
                    binding.viewError.isVisible = false
                    binding.viewLoading.isVisible = false
                    iconAdapter.set(it.value)
                }

                is UiState.Error -> {
                    Log.d("Log_tabbb", "initObserver: ${it.message}")
                    binding.viewError.isVisible = true
                    binding.viewLoading.isVisible = false
                }

                UiState.Loading -> {
                    binding.viewLoading.isVisible = true
                    binding.viewError.isVisible = false
                }

                else -> {}
            }
        }
    }


}
