package com.wavez.controllerwidget.ads

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ads.admob.native_ads.state.NativeAdState
import com.wavez.controllerwidget.core.base.BaseFragment
import com.google.android.gms.ads.nativead.NativeAd
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.databinding.ActivityFullScreenNativeAdBinding
import com.wavez.controllerwidget.ui.onboarding.WalkthroughActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FullScreenNativeAdFragment : BaseFragment<ActivityFullScreenNativeAdBinding>() {

    override fun bindingView(): ActivityFullScreenNativeAdBinding {
        return ActivityFullScreenNativeAdBinding.inflate(layoutInflater)
    }

    companion object {

        private const val ARG_POSITION = "native_ad_position"

        fun newInstance(position: NativeAdPlacement): FullScreenNativeAdFragment {
            return FullScreenNativeAdFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POSITION, position.name)
                }
            }
        }
    }

    private var hasTriggeredNextScreen = false
    private var nativeAdReady: NativeAd? = null

    private val adType: NativeAdPlacement by lazy {
        val name = arguments?.getString(ARG_POSITION)
        runCatching {
            NativeAdPlacement.valueOf(
                name ?: "",
            )
        }.getOrElse { NativeAdPlacement.ONBOARDING_FULL_1 }
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.btnClose.gone()
    }

    override fun initObserver() {
        super.initObserver()
        Log.d("Log_ad_full", "initObserver: $adType")
        nativeAdViewModel.ensureObserved(adType)
        nativeAdViewModel.getNativeFlowByPosition(adType).observe(viewLifecycleOwner) { state ->
            when (state) {
                is NativeAdState.Failed -> {
                    nativeAdReady = null
                }

                is NativeAdState.Loaded -> {
                    nativeAdReady = state.ad
                    if (nativeAdReady != null) {
                        binding.loadingLayout.gone()
                        binding.fullScreenNativeAdView.bind(nativeAdReady!!)
                    }
                }

                NativeAdState.Loading -> {}
            }
        }
    }

    fun onFragmentVisible() {
        if (hasTriggeredNextScreen) return
        if (nativeAdReady == null) {
            binding.root.postDelayed({
                if (!hasTriggeredNextScreen) {
                    hasTriggeredNextScreen = true
                    (activity as? WalkthroughActivity)?.nextScreen()
                }
            }, 300)
        }
    }

    fun runWhenViewResumed(action: () -> Unit) {
        if (!isAdded || view == null) {
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                action()
            }
        }
    }

}