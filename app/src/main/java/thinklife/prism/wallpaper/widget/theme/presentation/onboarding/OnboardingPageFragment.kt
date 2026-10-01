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

package thinklife.prism.wallpaper.widget.theme.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import thinklife.prism.wallpaper.widget.theme.ads.AdsSlot
import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.core.extensions.getParcel
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentOnboardingPageBinding
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.model.OnboardingModel

class OnboardingPageFragment : BaseFragment<FragmentOnboardingPageBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentOnboardingPageBinding.inflate(inflater, container, false)

    companion object {
        private const val ARG_DATA = "arg_data"
        private const val ARG_PAGE_INDEX = "arg_page_index"

        const val PAGE_ONB2 = 2
        const val PAGE_ONB3 = 3

        fun newInstance(onboardingModel: OnboardingModel, pageIndex: Int) = OnboardingPageFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_DATA, onboardingModel)
                putInt(ARG_PAGE_INDEX, pageIndex)
            }
        }
    }

    private var boardingData: OnboardingModel? = null

    private val pageIndex by lazy { arguments?.getInt(ARG_PAGE_INDEX) ?: PAGE_ONB2 }

    /** Một layout dùng cho 2 màn logic onb2/onb3 → screenId + tag slot gán động. */
    override val adsScreenId
        get() = if (pageIndex == PAGE_ONB3) AdsScreen.ONB3_VIEW else AdsScreen.ONB2_VIEW

    override fun onPrepareAdsSlots() {
        val prefix = if (pageIndex == PAGE_ONB3) AdsSlot.ONB3_SLOT_TOP else AdsSlot.ONB2_SLOT_TOP
        val bottom =
            if (pageIndex == PAGE_ONB3) AdsSlot.ONB3_SLOT_BOTTOM else AdsSlot.ONB2_SLOT_BOTTOM
        binding.onbSlotTop.tag = "ads_slot:$prefix"
        binding.onbSlotBottom.tag = "ads_slot:$bottom"
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        boardingData = arguments?.getParcel(ARG_DATA)
        boardingData?.let {
            initViewOnboardingAll(it)
        }
    }

    private fun initViewOnboardingAll(boardingData: OnboardingModel) {
        Glide.with(this).load(boardingData.image).placeholder(boardingData.image)
            .into(binding.ivImage)
        binding.tvTitle.text = getString(boardingData.title)
        if (boardingData.description != 0) {
            binding.tvDescription.text = getString(boardingData.description)
            binding.tvDescription.visibility = android.view.View.VISIBLE
        } else {
            binding.tvDescription.visibility = android.view.View.GONE
        }
    }

    override fun initListener() {
        super.initListener()

        binding.btnNext.setOnClickListener {
            val trigger =
                if (pageIndex == PAGE_ONB3) AdsTrigger.ONB3_CLICK_NEXT else AdsTrigger.ONB2_CLICK_NEXT
            triggerAds(trigger) {
                runWhenResumed { (activity as? OnboardingActivity)?.nextScreen() }
            }
        }
    }
}
