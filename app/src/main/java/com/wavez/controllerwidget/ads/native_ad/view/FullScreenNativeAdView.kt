package com.wavez.controllerwidget.ads.native_ad.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ads.admob.native_ads.adapter.NativeAdViewAd
import com.google.android.gms.ads.nativead.NativeAd
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.extensions.visible
import com.wavez.controllerwidget.databinding.LayoutFullScreenTemplateViewBinding

class FullScreenNativeAdView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), NativeAdViewAd {

    private val binding: LayoutFullScreenTemplateViewBinding =
        LayoutFullScreenTemplateViewBinding.inflate(
            LayoutInflater.from(context), this, false
        )

    init {
        addView(binding.root)
    }

    override var shouldShowAd: Boolean = true

    override fun bind(
        nativeAd: NativeAd,
    ) {
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
            binding.headLineView.text = nativeAd.headline
            binding.nativeAdView.headlineView = binding.headLineView
            binding.headLineView.visible()
        } else {
            binding.headLineView.gone()
        }

        val body = nativeAd.body
        if (body != null) {
            binding.contentView.text = nativeAd.body
            binding.nativeAdView.bodyView = binding.contentView
            binding.contentView.visible()
        } else {
            binding.contentView.gone()
        }

        val nativeIcon = nativeAd.icon
        if (nativeIcon == null) {
            binding.iconView.gone()
        } else {
            binding.iconView.setImageDrawable(nativeIcon.drawable)
            binding.nativeAdView.iconView = binding.iconView
            binding.iconView.visible()
        }

        binding.nativeAdView.setNativeAd(nativeAd)
    }

    override fun showPlaceholder() {
        binding.nativeAdView.visible()
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
}