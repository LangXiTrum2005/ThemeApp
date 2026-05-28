package com.wavez.controllerwidget.ui.onboarding.page

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.core.base.BaseFragment
import com.wavez.controllerwidget.core.extensions.getParcel
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.extensions.invisible
import com.wavez.controllerwidget.core.extensions.visible
import com.wavez.controllerwidget.core.utils.CustomToast
import com.wavez.controllerwidget.databinding.FragmentOnboardingDetailBinding
import com.wavez.controllerwidget.ui.onboarding.WalkthroughActivity
import com.wavez.controllerwidget.ui.onboarding.adapter.HabitsAdapter
import com.wavez.controllerwidget.ui.onboarding.model.BoardingData
import com.wavez.controllerwidget.ui.onboarding.provider.HabitsProvider
import com.wavez.controllerwidget.ui.utils.AppEvent
import com.wavez.controllerwidget.ui.utils.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WalkthroughDetailFragment : BaseFragment<FragmentOnboardingDetailBinding>() {
    override fun bindingView(): FragmentOnboardingDetailBinding {
        return FragmentOnboardingDetailBinding.inflate(layoutInflater)
    }

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

    private var isClickFirstOnboarding1: Boolean = false

    private val habitsAdapter by lazy {
        HabitsAdapter()
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        boardingData = arguments?.getParcel(ARG_DATA)
        boardingData?.let {
            initViewOnboardingAll(it)
        } ?: run {
        }
        initNativeAds()
        logEventScreen(boardingData)
    }

    private fun logEventScreen(boardingData: BoardingData?) {
        when (boardingData?.index) {
            1 -> {
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_VIEW)
            }

            2 -> {
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_2_VIEW)
            }

            3 -> {
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_3_VIEW)
            }

            else -> {

            }
        }
    }


    override fun initListener() {
        super.initListener()
        binding.btnNext.setOnClickListener {
            onNextPage()
        }
    }

    private fun onNextPage() {
        if (boardingData?.index == 1 && habitsAdapter.getSelectedItems().isEmpty()) {
            showToastFirstChoose()
        } else {
            if (boardingData?.index == 1) {
                HabitsProvider.pushEvent(habitsAdapter.getSelectedItems())
            }
            nextScreen()
        }
    }

    private fun initNativeAds() {
        setupNative(
            NativeAdPlacement.ONBOARDING1,
            binding.bigNativeAdView,
            arguments?.getBoolean(SHOULD_SHOW_NATIVE_AD) ?: true,
        )
    }

    private fun initNativeAds2() {
        binding.bigNativeClick.visible()
        setupNative(
            NativeAdPlacement.ONBOARDING1_CLICK,
            binding.bigNativeClick,
            sharedPref.shouldShowNativeObd1Click,
        )
    }

    private fun initViewOnboardingAll(boardingData: BoardingData) {
        if (boardingData.index == 1) {
            binding.viewObd1.root.visible()
            binding.bgObd.gone()
            binding.viewFake.invisible()
            initViewOnboarding1()
        } else {
            binding.viewFake.gone()
            binding.bgObd.visible()
            binding.btnNext.isSelected = true
            binding.viewObd1.root.gone()
            Glide.with(this).load(boardingData.imageID).placeholder(boardingData.imageID)
                .into(binding.imageIV)
            binding.tvHeader.text = getString(boardingData.labelID)
            binding.tvDes.text = getString(boardingData.desID)
        }
    }

    private fun initViewOnboarding1() {
        with(binding.viewObd1) {
            rcvHabits.adapter = habitsAdapter
            habitsAdapter.set(HabitsProvider.getHabits())
            habitsAdapter.onItemSelected = {
                val conditionAccessHabit = habitsAdapter.getSelectedItems().isNotEmpty()
                if (!isClickFirstOnboarding1) {
                    FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_CLICK)
                    initNativeAds2()
                    loadAd()
                    isClickFirstOnboarding1 = true
                }
                updateState(conditionAccessHabit)
            }
        }
    }

    private fun loadAd() {
        lifecycleScope.launch {
            nativeAdViewModel.loadAd(NativeAdPlacement.ONBOARDING_FULL_2, perLoadAfterClick = true)
        }
    }

    private fun updateState(conditionAccessHabit: Boolean) {
        binding.btnNext.isSelected = conditionAccessHabit
    }

    private fun showToastFirstChoose() {
        val toast = CustomToast.makeText(
            requireContext(),
            R.string.select_option,
            bgDrawable = R.drawable.bg_toas,
            gravity = Gravity.TOP,
            textColor = R.color.white,
        )
        toast.show()
    }


    private fun nextScreen() {
        when (boardingData?.index) {
            1 -> {
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_CLICK_NEXT)
            }

            2 -> {
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_2_CLICK_NEXT)
            }

            3 -> {
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_3_CLICK_NEXT)
            }
        }
        (activity as WalkthroughActivity).nextScreen()
    }
}
