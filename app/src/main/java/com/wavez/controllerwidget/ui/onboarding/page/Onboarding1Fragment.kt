package com.wavez.controllerwidget.ui.onboarding.page

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.core.base.BaseFragment
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.extensions.invisible
import com.wavez.controllerwidget.core.extensions.visible
import com.wavez.controllerwidget.core.utils.CustomToast
import com.wavez.controllerwidget.databinding.FragmentOnboarding1Binding
import com.wavez.controllerwidget.ui.onboarding.WalkthroughActivity
import com.wavez.controllerwidget.ui.onboarding.adapter.HabitsAdapter
import com.wavez.controllerwidget.ui.onboarding.provider.HabitsProvider
import com.wavez.controllerwidget.ui.utils.AppEvent
import com.wavez.controllerwidget.ui.utils.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Onboarding1Fragment : BaseFragment<FragmentOnboarding1Binding>() {

    override fun bindingView() = FragmentOnboarding1Binding.inflate(layoutInflater)

    companion object {
        private const val SHOULD_SHOW_NATIVE_AD = "should_show_native_ad"

        fun newInstance(shouldShowNativeAd: Boolean = false) =
            Onboarding1Fragment().apply {
                arguments = Bundle().apply {
                    putBoolean(SHOULD_SHOW_NATIVE_AD, shouldShowNativeAd)
                }
            }
    }

    private var isClickedFirst = false

    private val habitsAdapter by lazy { HabitsAdapter() }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.viewFake.invisible()
        initHabits()
        setupNative(
            NativeAdPlacement.ONBOARDING1,
            binding.bigNativeAdView,
            arguments?.getBoolean(SHOULD_SHOW_NATIVE_AD) ?: true,
            preloadOnClick = true
        )
        FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_VIEW)
    }

    override fun initListener() {
        super.initListener()
        binding.btnNext.setOnClickListener {
            if (habitsAdapter.getSelectedItems().isEmpty()) {
                showSelectToast()
            } else {
                HabitsProvider.pushEvent(habitsAdapter.getSelectedItems())
                FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_CLICK_NEXT)
                (activity as WalkthroughActivity).nextScreen()
            }
        }
    }

    private fun initHabits() {
        with(binding.viewObd1) {
            rcvHabits.adapter = habitsAdapter
            habitsAdapter.set(HabitsProvider.getHabits())
            habitsAdapter.onItemSelected = {
                if (!isClickedFirst) {
                    FirebaseControlLogEvent.logEvent(AppEvent.ONBOARDING_1_CLICK)
                    showClickNative()
                    preloadPage3Ad()
                    isClickedFirst = true
                }
                binding.btnNext.isSelected = habitsAdapter.getSelectedItems().isNotEmpty()
            }
        }
    }

    private fun showClickNative() {
        binding.bigNativeClick.visible()
        setupNative(
            NativeAdPlacement.ONBOARDING1_CLICK,
            binding.bigNativeClick,
            sharedPref.shouldShowNativeObd1Click,
            preloadOnClick = true
        )
    }

    private fun preloadPage3Ad() {
        loadNativeWithPosition(NativeAdPlacement.ONBOARDING3)
    }

    private fun showSelectToast() {
        CustomToast.makeText(
            requireContext(),
            R.string.select_option,
            bgDrawable = R.drawable.bg_toas,
            gravity = Gravity.TOP,
            textColor = R.color.white,
        ).show()
    }
}
