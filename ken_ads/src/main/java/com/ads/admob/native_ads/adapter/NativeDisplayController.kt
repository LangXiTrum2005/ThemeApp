package com.ads.admob.native_ads.adapter

import android.util.Log
import com.ads.admob.native_ads.state.NativeAdState
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.Job

class NativeDisplayController(
    private val nativeAdPlaceholder: NativeAdPlaceholder,
    private val nativeAdView: NativeAdViewAd,
) {

    private var loadAdJob: Job? = null

    fun setupAdDisplay(state: NativeAdState?) {
        if (state == null || !nativeAdView.shouldShowAd) {
            nativeAdView.hidePlaceholder()
            return
        }
        when (state) {
            is NativeAdState.Failed -> {
                nativeAdView.hidePlaceholder()
            }

            is NativeAdState.Loaded -> {
                val nativeAd = nativeAdPlaceholder.nativeAd
                Log.d("NativeDisplayController", "setupAdDisplay: $nativeAd")
                if (nativeAd != null) {
                    bind(nativeAd)
                } else {
                    nativeAdView.hidePlaceholder()
                }
            }

            NativeAdState.Loading -> {
                nativeAdView.showPlaceholder()
            }
        }

    }

    fun hideAd() {
        nativeAdView.hidePlaceholder()
    }

    private fun bind(nativeAd: NativeAd) {
        loadAdJob?.cancel()
        loadAdJob = null
        nativeAdView.showPlaceholder()
        nativeAdView.bind(nativeAd)
    }

    fun cleanupAdDisplay() {
        loadAdJob?.cancel()
        loadAdJob = null
        nativeAdPlaceholder.release()
    }
}