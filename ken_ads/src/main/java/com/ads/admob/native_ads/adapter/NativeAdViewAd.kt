package com.ads.admob.native_ads.adapter

import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

interface NativeAdViewAd {

    var shouldShowAd: Boolean

    fun bind(
        nativeAd: NativeAd,
    )

    fun showPlaceholder()

    fun hidePlaceholder()
}
