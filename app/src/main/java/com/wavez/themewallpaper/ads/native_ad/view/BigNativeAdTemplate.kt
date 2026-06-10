package com.wavez.themewallpaper.ads.native_ad.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ads.admob.native_ads.adapter.NativeAdViewAd
import com.google.android.gms.ads.nativead.NativeAd
import com.wavez.themewallpaper.core.extensions.gone
import com.wavez.themewallpaper.core.extensions.visible
import com.wavez.themewallpaper.databinding.LayoutBigTemplateViewBinding

class BigNativeAdTemplate @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs),
    NativeAdViewAd {

    private val binding: LayoutBigTemplateViewBinding =
        LayoutBigTemplateViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true,
        )

    override var shouldShowAd = true

    override fun bind(
        nativeAd: NativeAd,
    ) {
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
    }

    override fun showPlaceholder() {
        binding.nativeAdView.visible()
        binding.viewAd.gone()
        binding.shimmerNative.shimmerContainer.visible()
        binding.shimmerNative.shimmerNative.startShimmer()
    }

    private fun stopShimmerAd() {
        binding.shimmerNative.shimmerNative.stopShimmer()
        binding.shimmerNative.shimmerContainer.gone()
    }

    override fun hidePlaceholder() {
        this.gone()
    }
}