package com.ads.admob.native_ads.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.nativead.NativeAd

/**
 * Base class for all native ad templates.
 *
 * Subclasses must implement:
 * - [provideSlots] — return view references once (called lazily after construction)
 * - [onBind] — bind individual NativeAd fields to views
 *
 * [showPlaceholder], [hidePlaceholder], and [bind] are final — shimmer and
 * lifecycle are handled here so subclasses cannot diverge.
 */
abstract class BaseNativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), NativeAdViewAd {

    override var shouldShowAd: Boolean = true

    // Lazy: subclass binding initializes during subclass construction,
    // provideSlots() is only called on first external access (showPlaceholder/bind).
    private val slots: NativeAdSlots by lazy { provideSlots() }

    protected abstract fun provideSlots(): NativeAdSlots

    protected abstract fun onBind(nativeAd: NativeAd)

    final override fun showPlaceholder() {
        visibility = View.VISIBLE
        slots.nativeAdView.visibility = View.VISIBLE
        slots.contentView?.visibility = View.GONE
        slots.shimmerContainer.visibility = View.VISIBLE
        slots.shimmer.startShimmer()
    }

    final override fun hidePlaceholder() {
        visibility = View.GONE
    }

    final override fun bind(nativeAd: NativeAd) {
        visibility = View.VISIBLE          // ensure container is visible (may have been GONE)
        slots.shimmer.stopShimmer()
        slots.shimmerContainer.visibility = View.GONE
        slots.contentView?.visibility = View.VISIBLE
        slots.nativeAdView.visibility = View.VISIBLE
        onBind(nativeAd)
        slots.nativeAdView.setNativeAd(nativeAd)
    }
}
