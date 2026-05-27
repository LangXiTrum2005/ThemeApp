package com.ads.admob.native_ads.state

import com.google.android.gms.ads.nativead.NativeAd

sealed class NativeAdState {
    object Loading : NativeAdState()
    data class Loaded(val ad: NativeAd?) : NativeAdState()
    data class Failed(val errorCode: Int? = null) : NativeAdState()
}