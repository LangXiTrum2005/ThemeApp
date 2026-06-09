package com.ads.admob.native_ads.adapter

import com.google.android.gms.ads.nativead.NativeAd

/**
 * Translates [NativeAdLoadState] into view calls on [NativeAdViewAd].
 *
 * Repository is the sole owner of [NativeAd] lifecycle — this controller
 * only tracks which ad is currently bound (for deduplication) and never
 * calls [NativeAd.destroy].
 */
class NativeDisplayController(private val view: NativeAdViewAd) {

    private var boundAd: NativeAd? = null

    fun onStateChanged(state: NativeAdLoadState) {
        if (!view.shouldShowAd) {
            view.hidePlaceholder()
            return
        }
        when (state) {
            is NativeAdLoadState.Loaded -> {
                if (boundAd !== state.ad) {
                    boundAd = state.ad
                    view.bind(state.ad) // bind() makes the view visible and stops shimmer
                }
            }
            is NativeAdLoadState.Loading,
            is NativeAdLoadState.AdClicked -> {
                boundAd = null
                view.showPlaceholder()
            }
            is NativeAdLoadState.Failed,
            is NativeAdLoadState.Idle -> {
                view.hidePlaceholder()
            }
        }
    }

    fun cleanup() {
        boundAd = null
    }
}
