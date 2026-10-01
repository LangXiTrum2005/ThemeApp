package com.wavez.themewallpaper.ui.onboarding.page

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.base.BaseFragment
import com.wavez.themewallpaper.core.extensions.getParcel
import com.wavez.themewallpaper.databinding.FragmentOnboardingDetailBinding
import com.wavez.themewallpaper.ui.onboarding.WalkthroughActivity
import com.wavez.themewallpaper.ui.onboarding.model.BoardingData
import com.wavez.themewallpaper.ui.utils.AppEvent
import com.wavez.themewallpaper.ui.utils.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalkthroughDetailFragment : BaseFragment<FragmentOnboardingDetailBinding>() {

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
            logNextEvent()
            (activity as WalkthroughActivity).nextScreen()
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
