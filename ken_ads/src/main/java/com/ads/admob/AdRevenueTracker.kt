package com.ads.admob

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustEvent
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd

object AdRevenueTracker {

    fun trackAdRevenue(ad: Any) {
        when (ad) {
            is InterstitialAd -> handlePaidEvent(ad)
            is AdView -> handlePaidEvent(ad)
            is AppOpenAd -> handlePaidEvent(ad)
            is NativeAd -> handlePaidEvent(ad)
            is RewardedAd -> handlePaidEvent(ad)
        }
    }

    private fun handlePaidEvent(ad: InterstitialAd) {
        ad.setOnPaidEventListener { adValue ->
            processAdRevenue(adValue, ad.responseInfo.loadedAdapterResponseInfo?.adSourceName)
        }
    }

    private fun handlePaidEvent(ad: AdView) {
        ad.setOnPaidEventListener { adValue ->
            processAdRevenue(adValue, ad.responseInfo?.loadedAdapterResponseInfo?.adSourceName)
        }
    }

    private fun handlePaidEvent(ad: AppOpenAd) {
        ad.setOnPaidEventListener { adValue ->
            processAdRevenue(adValue, ad.responseInfo.loadedAdapterResponseInfo?.adSourceName)
        }
    }

    private fun handlePaidEvent(ad: NativeAd) {
        ad.setOnPaidEventListener { adValue ->
            processAdRevenue(adValue, ad.responseInfo?.loadedAdapterResponseInfo?.adSourceName)
        }
    }

    private fun handlePaidEvent(ad: RewardedAd) {
        ad.setOnPaidEventListener { adValue ->
            processAdRevenue(adValue, ad.responseInfo.loadedAdapterResponseInfo?.adSourceName)
        }
    }

    private fun processAdRevenue(adValue: AdValue, adSourceName: String?) {
        val revenue = adValue.valueMicros * 1.0 / 1_000_000.0
        val adRevenue = AdjustAdRevenue("admob_sdk").apply {
            setRevenue(revenue, adValue.currencyCode)
            adRevenueNetwork = adSourceName
        }
        Adjust.trackAdRevenue(adRevenue)

        val event = AdjustEvent("ad_impression_event").apply {
            setRevenue(revenue, adValue.currencyCode)
            addPartnerParameter("AD_IMPRESSION", "Purchase")
        }
        Adjust.trackEvent(event)
    }
}
