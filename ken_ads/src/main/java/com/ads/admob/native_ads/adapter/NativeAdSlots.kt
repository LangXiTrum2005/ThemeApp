package com.ads.admob.native_ads.adapter

import android.view.View
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * View references required by [BaseNativeAdView].
 * Subclasses return this from [BaseNativeAdView.provideSlots].
 *
 * @param contentView Separate content wrapper distinct from [nativeAdView].
 *   Null for layouts where [nativeAdView] is the direct content root.
 */
data class NativeAdSlots(
    val nativeAdView: NativeAdView,
    val shimmer: ShimmerFrameLayout,
    val shimmerContainer: View,
    val contentView: View? = null,
)
