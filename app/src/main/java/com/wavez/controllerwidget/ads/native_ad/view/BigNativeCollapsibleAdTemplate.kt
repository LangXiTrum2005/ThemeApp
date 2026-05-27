package com.wavez.controllerwidget.ads.native_ad.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ads.admob.native_ads.adapter.CollapsibleAd
import com.ads.admob.native_ads.adapter.NativeAdViewAd
import com.google.android.gms.ads.nativead.NativeAd
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.extensions.visible
import com.wavez.controllerwidget.databinding.LayoutBigCollpasibleTemplateViewBinding

class BigNativeCollapsibleAdTemplate @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs),
    NativeAdViewAd, CollapsibleAd {

    private val binding: LayoutBigCollpasibleTemplateViewBinding =
        LayoutBigCollpasibleTemplateViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true,
        )

    override var shouldCollapseRate: Boolean = true

    private var nativeAdViewIsBound = false

    override var shouldShowAd = true
    override fun bind(
        nativeAd: NativeAd,
    ) {
        nativeAdViewIsBound = true
        binding.viewAd.visible()
        stopShimmerAd()

        val mediaContext = nativeAd.mediaContent
        if (mediaContext != null) {
            binding.mediaView.mediaContent = mediaContext
            binding.nativeAdView.mediaView = binding.mediaView
            binding.mediaView.visible()
        } else {
            binding.mediaView.gone()
        }

        val callToAction = nativeAd.callToAction
        if (callToAction != null) {
            binding.callToActionView.text = nativeAd.callToAction
            binding.nativeAdView.callToActionView = binding.callToActionView
            binding.callToActionView.visible()
        } else {
            binding.callToActionView.gone()
        }

        val headline = nativeAd.headline
        if (headline != null) {
            binding.headlineView.text = nativeAd.headline
            binding.nativeAdView.headlineView = binding.headlineView
            binding.headlineView.visible()
        } else {
            binding.headlineView.gone()
        }

        binding.nativeAdView.setNativeAd(nativeAd)

        binding.btnCollapse.setOnClickListener {
            hidePlaceholder()
        }
    }


    override fun showPlaceholder() {
        binding.nativeAdView.visible()
        binding.viewAd.gone()
        binding.shimmerNative.shimmerContainer.visible()
        binding.shimmerNative.shimmerNative.startShimmer()
    }

    override fun hidePlaceholder() {
        binding.nativeAdView.gone()
    }

    private fun stopShimmerAd() {
        binding.shimmerNative.shimmerNative.stopShimmer()
        binding.shimmerNative.shimmerContainer.gone()
    }

    fun performClickAd() {
        binding.nativeAdView.callToActionView?.callOnClick()
    }

}
