package com.ads.admob.native_ads.adapter

sealed class NativeAdLoadState {
    data object Idle : NativeAdLoadState()
    data object Loading : NativeAdLoadState()
    data object Loaded : NativeAdLoadState()
    data class Failed(val errorMessage: String) : NativeAdLoadState()
    data object AdClicked : NativeAdLoadState()
}
