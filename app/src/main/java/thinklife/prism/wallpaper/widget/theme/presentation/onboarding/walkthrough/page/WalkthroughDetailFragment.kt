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

package thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.page

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.core.extensions.getParcel
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentOnboardingDetailBinding
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.WalkthroughActivity
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.model.BoardingData
import thinklife.prism.wallpaper.widget.theme.util.AppEvent
import thinklife.prism.wallpaper.widget.theme.util.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalkthroughDetailFragment : BaseFragment<FragmentOnboardingDetailBinding>() {

    override val adsScreenId = AdsScreen.WALKTHROUGH_DETAIL_VIEW


    override fun bindingView() = FragmentOnboardingDetailBinding.inflate(layoutInflater)

    companion object {
        private const val ARG_DATA = "arg_data"

        fun newInstance(boardingData: BoardingData? = null) =
            WalkthroughDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_DATA, boardingData)
                }
            }
    }

    private var boardingData: BoardingData? = null

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        boardingData = arguments?.getParcel(ARG_DATA)
        boardingData?.let { initView(it) }
        logViewEvent()
    }

    override fun initListener() {
        super.initListener()
        binding.btnNext.setOnClickListener {
            triggerAds(AdsTrigger.WALKTHROUGH_DETAIL_CLICK_NEXT) {
                runWhenResumed {
                    logNextEvent()
                    (activity as WalkthroughActivity).nextScreen()
                }
            }
        }
    }

    private fun initView(data: BoardingData) {
        Glide.with(this).load(data.imageID).placeholder(data.imageID).into(binding.imageIV)
        binding.tvHeader.text = getString(data.labelID)
        binding.tvDes.text = getString(data.desID)
        binding.tvNext.text = getString(if (data.index == 3) R.string.get_start else R.string.next)
    }

    private fun logViewEvent() {
        when (boardingData?.index) {
            2 -> FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_2_VIEW)
            3 -> FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_3_VIEW)
        }
    }

    private fun logNextEvent() {
        when (boardingData?.index) {
            2 -> FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_2_CLICK_NEXT)
            3 -> FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_3_CLICK_NEXT)
        }
    }
}
