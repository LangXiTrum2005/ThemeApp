package com.wavez.controllerwidget.ui.onboarding.page

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.core.base.BaseFragment
import com.wavez.controllerwidget.core.extensions.getParcel
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.extensions.visible
import com.wavez.controllerwidget.databinding.FragmentOnboardingDetailBinding
import com.wavez.controllerwidget.ui.onboarding.WalkthroughActivity
import com.wavez.controllerwidget.ui.onboarding.model.BoardingData
import com.wavez.controllerwidget.ui.utils.AppEvent
import com.wavez.controllerwidget.ui.utils.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalkthroughDetailFragment : BaseFragment<FragmentOnboardingDetailBinding>() {

    override fun bindingView() = FragmentOnboardingDetailBinding.inflate(layoutInflater)

    companion object {
        private const val ARG_DATA = "arg_data"
        private const val SHOULD_SHOW_NATIVE_AD = "should_show_native_ad"

        fun newInstance(boardingData: BoardingData? = null, shouldShowNativeAd: Boolean = false) =
            WalkthroughDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_DATA, boardingData)
                    putBoolean(SHOULD_SHOW_NATIVE_AD, shouldShowNativeAd)
                }
            }
    }

    private var boardingData: BoardingData? = null

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        boardingData = arguments?.getParcel(ARG_DATA)
        boardingData?.let { initView(it) }
        initNativeAds()
        logViewEvent()
    }

    override fun initListener() {
        super.initListener()
        binding.btnNext.setOnClickListener {
            logNextEvent()
            (activity as WalkthroughActivity).nextScreen()
        }
    }

    private fun initView(data: BoardingData) {
        Glide.with(this).load(data.imageID).placeholder(data.imageID).into(binding.imageIV)
        binding.tvHeader.text = getString(data.labelID)
        binding.tvDes.text = getString(data.desID)
    }

    private fun initNativeAds() {
        val show = arguments?.getBoolean(SHOULD_SHOW_NATIVE_AD) ?: true
        if (boardingData?.index == 2) {
            binding.bigNative2.gone()
            setupNative(NativeAdPlacement.ONBOARDING2, binding.bigNative1, show)
        } else {
            binding.bigNative1.gone()
            setupNative(NativeAdPlacement.ONBOARDING3, binding.bigNative2, show)
        }
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
