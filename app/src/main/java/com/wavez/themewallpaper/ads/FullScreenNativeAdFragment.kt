package com.wavez.themewallpaper.ads

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ads.admob.MMonetize
import com.ads.admob.native_ads.adapter.NativeAdLoadState
import com.wavez.themewallpaper.ads.native_ad.NativeAdPlacement
import com.wavez.themewallpaper.core.base.BaseFragment
import com.wavez.themewallpaper.core.extensions.gone
import com.wavez.themewallpaper.databinding.ActivityFullScreenNativeAdBinding
import com.wavez.themewallpaper.ui.onboarding.WalkthroughActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
    private var isAdReady = false

    private val adType: NativeAdPlacement by lazy {
        val name = arguments?.getString(ARG_POSITION)
        runCatching { NativeAdPlacement.valueOf(name ?: "") }
            .getOrElse { NativeAdPlacement.ONBOARDING_FULL_1 }
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.btnClose.gone()
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MMonetize.nativeAdRepository.getAdStateFlow(adType.name).collectLatest { state ->
                    when (state) {
                        is NativeAdLoadState.Loaded -> {
                            isAdReady = true
                            binding.loadingLayout.gone()
                            binding.fullScreenNativeAdView.bind(state.ad)
                        }

                        is NativeAdLoadState.Failed -> isAdReady = false
                        else -> {}
                    }
                }
            }
        }
    }

    fun onFragmentVisible() {
        if (hasTriggeredNextScreen) return
        if (!isAdReady) {
            binding.root.postDelayed({
                if (!hasTriggeredNextScreen) {
                    hasTriggeredNextScreen = true
                    (activity as? WalkthroughActivity)?.nextScreen()
                }
            }, 300)
        }
    }

    fun runWhenViewResumed(action: () -> Unit) {
        if (!isAdded || view == null) return
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                action()
            }
        }
    }

    override fun onDestroyView() {
        MMonetize.nativeAdRepository.releaseAd(adType.name)
        super.onDestroyView()
    }
}
