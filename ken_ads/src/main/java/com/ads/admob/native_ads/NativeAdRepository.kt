package com.ads.admob.native_ads

import com.ads.admob.LoadCondition
import com.ads.admob.native_ads.adapter.NativeAdLoadState
import kotlinx.coroutines.flow.StateFlow

interface NativeAdRepository {

    fun setNativeAdIds(adPlacement: String, ids: List<String>)

    fun setLoadCondition(condition: LoadCondition?)

    fun setPreloadOnClick(adPlacement: String, enabled: Boolean)

    fun loadAd(adPlacement: String, forceReload: Boolean = false)

    fun getAdStateFlow(adPlacement: String): StateFlow<NativeAdLoadState>

    fun releaseAd(adPlacement: String)

    fun clearAds(adPlacement: String)
}
