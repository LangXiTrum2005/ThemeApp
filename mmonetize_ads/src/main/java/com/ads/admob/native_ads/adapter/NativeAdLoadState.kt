package com.ads.admob.native_ads.adapter

import com.google.android.gms.ads.nativead.NativeAd

sealed class NativeAdLoadState {
    data object Idle : NativeAdLoadState()
    data object Loading : NativeAdLoadState()
    data class Loaded(val ad: NativeAd) : NativeAdLoadState()
    data class Failed(val errorMessage: String = "") : NativeAdLoadState()
    data object AdClicked : NativeAdLoadState()
}
