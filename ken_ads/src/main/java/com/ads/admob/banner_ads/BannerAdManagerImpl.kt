package com.ads.admob.banner_ads

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.ads.admob.AdRevenueTracker
import com.ads.admob.GoogleMobileAdsConsentManager
import com.ads.admob.LoadCondition
import com.ads.admob.ShowCondition
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Loads a banner ad with the specified ad unit ID. If adIdHigh is provided and fails,
 * it falls back to adId. Supports collapsible ads with optional load and show conditions.
 *
 * @param adId The default ad unit ID.
 * @param adIdHigh The high-priority ad unit ID (optional).
 * @param adSize The size of the ad.
 * @param collapsible Whether the ad is collapsible.
 * @param onAdLoaded Callback invoked when the ad is successfully loaded.
 * @param onAdFailedToLoad Callback invoked when the ad fails to load.
 */

class BannerAdManagerImpl(
    private val context: Context
) : BannerAdManager {

    companion object {
        private const val TAG = "BannerAdManager"
    }

    private var adView: AdView? = null
    private var isLoading = false
    private var additionalLoadCondition: LoadCondition? = null
    private var additionalShowCondition: ShowCondition? = null
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)

    private fun isAdEligibleToLoad(): Boolean {
        if (!googleMobileAdsConsentManager.canRequestAds) return false
        if (isLoading) return false
        val additionalLoadCondition = additionalLoadCondition
        if (additionalLoadCondition != null && !additionalLoadCondition.shouldLoad()) {
            return false
        }
        val additionalShowCondition = additionalShowCondition
        if (additionalShowCondition != null) {
            return additionalShowCondition.shouldShow()
        }

        return true
    }


    override fun loadBannerAd(
        adId: String,
        adIdHigh: String?,
        adSize: AdSizeBanner,
        collapsible: Boolean,
        onAdLoaded: ((AdView) -> Unit)?,
        onAdFailedToLoad: (() -> Unit)?
    ) {
        val adUnitId = adIdHigh.takeIf { !it.isNullOrEmpty() } ?: adId
        Log.d("log_banner", "loadBannerAd: $adUnitId")
        if (!isAdEligibleToLoad()) {
            onAdFailedToLoad?.invoke()
            return
        }
        if (isLoading) {
            return
        }
        isLoading = true
        val adView = AdView(context)
        adView.setAdSize(adSize.toAdSize(context))
        adView.adUnitId = adUnitId
        Log.d(TAG, "adUnitId: $adUnitId")
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "onAdLoaded: $adView --- $adIdHigh")
                isLoading = false
                AdRevenueTracker.trackAdRevenue(adView)
                onAdLoaded?.invoke(adView)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                isLoading = false
                this@BannerAdManagerImpl.adView = null
                Log.d("log_banner_ad", "adError: ${adError.message} -- $adIdHigh")

                if (adIdHigh.isNullOrEmpty()) {
                    Log.d("log_banner_ad", "onAdFailedToLoad: ")
                    onAdFailedToLoad?.invoke()
                } else {
                    Log.d("log_banner_ad", "onAdFailedToLoad: loadBannerAd ")
                    loadBannerAd(adId, null, adSize, collapsible, onAdLoaded, onAdFailedToLoad)
                }
            }
        }
        this.adView = adView
        val adRequest = AdRequest.Builder()
        if (collapsible) {
            val extras = Bundle()
            extras.putString("collapsible", "bottom")
            adRequest.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }
        adView.loadAd(adRequest.build())
    }

    override fun destroyBannerAd() {
        adView?.destroy()
        adView = null
    }

    override fun onPauseBannerAd() {
        adView?.pause()
    }

    override fun onResumeBannerAd() {
        adView?.resume()
    }

    override fun setLoadCondition(condition: LoadCondition?) {
        additionalLoadCondition = condition
    }

    override fun setShowCondition(condition: ShowCondition?) {
        additionalShowCondition = condition
    }
}
