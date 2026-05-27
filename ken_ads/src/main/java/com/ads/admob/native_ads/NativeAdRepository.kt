package com.ads.admob.native_ads

import com.ads.admob.LoadCondition
import com.ads.admob.ShowCondition
import com.ads.admob.native_ads.adapter.NativeAdLoadState
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.flow.StateFlow

interface NativeAdRepository {

    var preloadCount: Int

    fun setPreloadCount(adPlacement: String, count: Int)

    fun loadAd(adPlacement: String, perLoadAfterClick: Boolean = false)

    fun setNativeAdIds(adPlacement: String, ids: List<String>)

    fun getLoadedNativeAd(adPlacement: String): NativeAd?

    fun getNativeAdLoadFlow(adPlacement: String): StateFlow<NativeAdLoadState>

    fun clearAds(adPlacement: String)

    fun setLoadCondition(condition: LoadCondition?)

    fun setShowCondition(condition: ShowCondition?)
}
