package com.ads.admob.open_ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.ads.admob.AdLoadFailureDelay
import com.ads.admob.AdRevenueTracker
import com.ads.admob.LoadCondition
import com.ads.admob.ShowCondition
import com.ads.admob.AdsManagerState
import com.ads.admob.GoogleMobileAdsConsentManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean

class OpenAdManagerImpl(private val context: Context) : OpenAdManager {

    private var isLoading = false
    private var appOpenAd: AppOpenAd? = null
    private val loadFailureDelay = AdLoadFailureDelay()
    private var _adUnitId: String? = null
    private var additionalLoadCondition: LoadCondition? = null
    private var additionalShowCondition: ShowCondition? = null
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)
    override var dismissFullScreenLastTime = 0L

    private val _state = MutableStateFlow(OpenAdState.DISMISSED)
    override val state: StateFlow<OpenAdState>
        get() = _state

    private fun isAdEligibleToLoad(): Boolean {
        val additionalLoadCondition = additionalLoadCondition
        if (additionalLoadCondition != null && !additionalLoadCondition.shouldLoad()) {
            return false
        }

        if (isLoading || isAdReady() || loadFailureDelay.shouldDelayRetry() || !googleMobileAdsConsentManager.canRequestAds) {
            return false
        }

        return true
    }

    override fun loadAd() {
        val adUnitId = _adUnitId ?: throw Exception("Ad unit ID must be set")
        if (!isAdEligibleToLoad()) return
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(context, adUnitId, adRequest, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.d("log_test_123", "onAdLoaded: $ad")
                AdRevenueTracker.trackAdRevenue(ad)
                setLoadedAd(ad)
                loadFailureDelay.resetFailureCount()
                finishLoading()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d("log_test_123", "onAdFailedToLoad cause: ${loadAdError.cause}")
                Log.d("log_test_123", "onAdFailedToLoad message: ${loadAdError.message}")
                loadFailureDelay.recordFailedLoad()
                finishLoading()
            }
        })
    }

    override fun setAdUnitId(adUnitId: String) {
        _adUnitId = adUnitId
    }

    private fun finishLoading() {
        isLoading = false
    }

    private fun setLoadedAd(ad: AppOpenAd) {
        appOpenAd = ad
    }

    override fun isAdReady(): Boolean {
        return appOpenAd != null
    }

    override fun show(activity: Activity, onContinueTask: (() -> Unit)?) {

        if (AdsManagerState.consumeDisableOpenAd()) {
            onContinueTask?.invoke()
            return
        }

        if (appOpenAd == null) {
            loadAd()
            onContinueTask?.invoke()
            return
        }

        if (AdsManagerState.isAdShowFullScreen) {
            onContinueTask?.invoke()
            return
        }

        val showCondition = additionalShowCondition
        if (showCondition != null && !showCondition.shouldShow()){
            onContinueTask?.invoke()
            return
        }

        val ad = appOpenAd
        loadNewAd()
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {
                    _state.value = OpenAdState.SHOWING
                    AdsManagerState.onShowFullScreen()
                    super.onAdShowedFullScreenContent()
                }

                override fun onAdDismissedFullScreenContent() {
                    _state.value = OpenAdState.DISMISSED
                    dismissFullScreenLastTime = System.currentTimeMillis()
                    AdsManagerState.onDismissFullScreen()
                    onContinueTask?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    _state.value = OpenAdState.DISMISSED
                    AdsManagerState.onDismissFullScreen()
                    onContinueTask?.invoke()
                }
            }
            ad.show(activity)
        } else {
            onContinueTask?.invoke()
        }
    }

    private fun loadNewAd() {
        appOpenAd = null
        loadAd()
    }

    override fun setLoadCondition(condition: LoadCondition?) {
        additionalLoadCondition = condition
    }

    override fun setShowCondition(condition: ShowCondition?) {
        additionalShowCondition = condition
    }

}


