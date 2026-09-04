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

package com.onetabb.template.android.presentation.main.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.onetabb.template.android.R
import com.onetabb.template.android.ads.AdsScreen
import com.onetabb.template.android.ads.AdsSlot
import com.onetabb.template.android.ads.AdsTrigger
import com.onetabb.template.android.core.base.BaseFragment
import com.onetabb.template.android.databinding.FragmentTabPlaceholderBinding
import com.onetabb.template.android.presentation.language.SettingLanguageActivity
import com.onetabb.template.android.presentation.main.MainPage
import com.ontabb.rate.OneTabbRate

/**
 * Fragment rỗng cho 4 tab của MainActivity — chỉ giữ đúng khung ads và screenId.
 *
 * TODO(project): xoá file này, viết Fragment thật cho từng tab. Giữ nguyên 2 điểm: override
 * `adsScreenId` và để 2 slot top/bottom neo cứng ngoài vùng cuộn. Tab Settings nên có nút
 * "Rate us" gọi `OneTabbRate.showOnDemand(requireActivity())` — bung thẳng, bỏ qua bộ đếm lẫn
 * cờ "đã đánh giá rồi".
 */
class TabPlaceholderFragment : BaseFragment<FragmentTabPlaceholderBinding>() {

    companion object {
        private const val ARG_PAGE = "arg_page"

        fun newInstance(page: Int) = TabPlaceholderFragment().apply {
            arguments = Bundle().apply { putInt(ARG_PAGE, page) }
        }
    }

    private val page by lazy { arguments?.getInt(ARG_PAGE) ?: MainPage.HOME }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentTabPlaceholderBinding.inflate(inflater, container, false)

    override val adsScreenId
        get() = when (page) {
            MainPage.TAB2 -> AdsScreen.TAB2_VIEW
            MainPage.TAB3 -> AdsScreen.TAB3_VIEW
            MainPage.SETTINGS -> AdsScreen.SETTINGS_VIEW
            else -> AdsScreen.HOME_VIEW
        }

    /** Một layout dùng cho 4 màn logic → tag slot phải gán động trước khi bind. */
    override fun onPrepareAdsSlots() {
        val (top, bottom) = when (page) {
            MainPage.TAB2 -> AdsSlot.TAB2_SLOT_TOP to AdsSlot.TAB2_SLOT_BOTTOM
            MainPage.TAB3 -> AdsSlot.TAB3_SLOT_TOP to AdsSlot.TAB3_SLOT_BOTTOM
            MainPage.SETTINGS -> AdsSlot.SETTINGS_SLOT_TOP to AdsSlot.SETTINGS_SLOT_BOTTOM
            else -> AdsSlot.HOME_SLOT_TOP to AdsSlot.HOME_SLOT_BOTTOM
        }
        binding.tabSlotTop.tag = "ads_slot:$top"
        binding.tabSlotBottom.tag = "ads_slot:$bottom"
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        binding.tvTitle.setText(
            when (page) {
                MainPage.TAB2 -> R.string.tab_2
                MainPage.TAB3 -> R.string.tab_3
                MainPage.SETTINGS -> R.string.tab_settings
                else -> R.string.tab_home
            },
        )
        val isSettings = page == MainPage.SETTINGS
        binding.btnLanguage.isVisible = isSettings
        binding.btnRateUs.isVisible = isSettings
    }

    override fun initListener() {
        binding.btnLanguage.setOnClickListener {
            triggerAds(AdsTrigger.SETTINGS_CLICK_LANGUAGE) {
                runWhenResumed { startActivity(SettingLanguageActivity.newIntent(requireContext())) }
            }
        }
        binding.btnRateUs.setOnClickListener {
            triggerAds(AdsTrigger.SETTINGS_CLICK_RATE) {
                // User tự bấm nên bung thẳng: showOnDemand bỏ qua cả bộ đếm lẫn cờ "đã đánh giá
                // rồi". Không bung được (state saved) thì mở luôn trang app trên Play Store.
                runWhenResumed {
                    if (!OneTabbRate.showOnDemand(requireActivity())) {
                        OneTabbRate.openStoreListing(requireContext())
                    }
                }
            }
        }
    }
}
