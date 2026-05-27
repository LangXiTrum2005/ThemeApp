package com.ads.admob.native_ads.adapter

import com.ads.admob.native_ads.NativeAdRepository
import com.google.android.gms.ads.nativead.NativeAd

class NativeAdPlaceholder(
    private val nativeAdRepository: NativeAdRepository,
    private val adPlacement: String,
    cachedAd: NativeAd? = null,
) {

    // Resolve exactly once: dùng cachedAd nếu có, không thì lấy từ repository
    val nativeAd: NativeAd? = cachedAd ?: nativeAdRepository.getLoadedNativeAd(adPlacement)

    fun release() {
        nativeAd?.destroy()
    }
}