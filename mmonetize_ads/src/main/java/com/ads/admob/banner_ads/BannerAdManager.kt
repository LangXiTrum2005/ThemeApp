package com.ads.admob.banner_ads

import com.ads.admob.LoadCondition
import com.ads.admob.ShowCondition
import com.google.android.gms.ads.AdView

interface BannerAdManager {

    fun loadBannerAd(
        adId: String,
        adIdHigh: String? = null,
        adSize: AdSizeBanner = AdSizeBanner.BANNER,
        collapsible: Boolean = false,
        onAdLoaded: ((AdView) -> Unit)? = null,
        onAdFailedToLoad: (() -> Unit)? = null
    )

    fun destroyBannerAd()

    fun onResumeBannerAd()

    fun onPauseBannerAd()

    fun setLoadCondition(condition: LoadCondition?)

    fun setShowCondition(condition: ShowCondition?)
}