package com.ads.admob.interstitial_ads

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.ads.admob.AdLoadFailureDelay
import com.ads.admob.AdLoadingDialog
import com.ads.admob.AdRevenueTracker
import com.ads.admob.AdsManagerState
import com.ads.admob.GoogleMobileAdsConsentManager
import com.ads.admob.LoadCondition
import com.ads.admob.ShowCondition
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdInterstitialManagerImpl(
    private val context: Context
) : AdInterstitialManager {

    companion object {
        private const val SHOW_LOADING_DELAY_MS = 1000L
    }

    private var adUnitIds: List<String> = emptyList()
    private var currentAdIndex = 0
    private val loadFailureDelay = AdLoadFailureDelay()
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    override var dismissFullScreenLastTime: Long = 0L
    private var additionalLoadCondition: LoadCondition? = null
    private var additionalShowCondition: ShowCondition? = null
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)

    private var adLoadingDialog: AdLoadingDialog? = null
    private val loadingHandler = Handler(Looper.getMainLooper())
    private var loadingRunnable: Runnable? = null

    override fun isAdReady(): Boolean = interstitialAd != null

    override fun isAdEligibleToShow(): Boolean {
        if (interstitialAd == null) {
            loadAd()
            return false
        }

        if (AdsManagerState.isAdShowFullScreen) return false
        additionalShowCondition?.let {
            if (!it.shouldShow()) return false
        }

        return true
    }

    private fun isAdEligibleToLoad(): Boolean {
        if (!googleMobileAdsConsentManager.canRequestAds) return false
        if (isAdReady()) return false
        if (isLoading) return false
        additionalLoadCondition?.let { return it.shouldLoad() }
        return true
    }

    override fun loadAd() {
        if (adUnitIds.isEmpty()) return
        if (!isAdEligibleToLoad()) return

        if (currentAdIndex >= adUnitIds.size) {
            currentAdIndex = 0
            return
        }

        val adUnitId = adUnitIds[currentAdIndex]
        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    AdRevenueTracker.trackAdRevenue(ad)
                    loadFailureDelay.resetFailureCount()
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.d("AdInterstitialManager", "onAdFailedToLoad ($adUnitId): ${error.message}")
                    isLoading = false
                    currentAdIndex++
                    loadAd()
                }
            }
        )
    }

    override fun setAdUnitIds(ids: List<String>) {
        if (ids.isEmpty()) throw Exception("Ad unit ids cannot be empty")
        adUnitIds = ids
        currentAdIndex = 0
    }

    override fun showAdFullScreen(
        activity: Activity,
        showLoading: Boolean,
        loadingView: View?,
        preloadOnShow: Boolean,
        onAdComplete: () -> Unit
    ) {
        if (!isAdEligibleToShow()) {
            onAdComplete()
            return
        }

        if (showLoading) {
            showWithLoading(activity, loadingView, preloadOnShow, onAdComplete)
        } else {
            showNow(activity, preloadOnShow, onAdComplete)
        }
    }

    override fun forceShowAdFullScreen(
        activity: Activity,
        showLoading: Boolean,
        loadingView: View?,
        preloadOnShow: Boolean,
        onAdComplete: () -> Unit
    ) {
        if (interstitialAd == null) {
            onAdComplete()
            return
        }

        if (showLoading) {
            showWithLoading(activity, loadingView, preloadOnShow, onAdComplete)
        } else {
            showNow(activity, preloadOnShow, onAdComplete)
        }
    }

    private fun showNow(activity: Activity, preloadOnShow: Boolean, onAdComplete: () -> Unit) {
        if (!isActivityValid(activity)) {
            onAdComplete()
            return
        }
        interstitialAd?.let { ad ->
            interstitialAd = null
            if (preloadOnShow) loadAd()
            setFullScreenContentCallback(ad, onAdComplete)
            ad.show(activity)
        } ?: onAdComplete()
    }

    private fun showWithLoading(
        activity: Activity,
        loadingView: View?,
        preloadOnShow: Boolean,
        onAdComplete: () -> Unit
    ) {
        if (!isActivityValid(activity)) {
            onAdComplete()
            return
        }

        try {
            adLoadingDialog = AdLoadingDialog(activity, loadingView)
            adLoadingDialog?.show()
        } catch (e: Exception) {
            Log.w("AdInterstitialManager", "Failed to show loading dialog: ${e.message}")
            onAdComplete()
            return
        }

        loadingRunnable?.let { loadingHandler.removeCallbacks(it) }
        loadingRunnable = Runnable {
            if (!isActivityValid(activity)) {
                dismissLoadingDialog()
                onAdComplete()
                return@Runnable
            }

            interstitialAd?.let { ad ->
                interstitialAd = null
                if (preloadOnShow) loadAd()
                setFullScreenContentCallback(ad) {
                    dismissLoadingDialog()
                    onAdComplete()
                }
                try {
                    ad.show(activity)
                } catch (e: Exception) {
                    Log.w("AdInterstitialManager", "Failed to show interstitial: ${e.message}")
                    dismissLoadingDialog()
                    onAdComplete()
                }
            } ?: run {
                dismissLoadingDialog()
                onAdComplete()
            }
        }
        loadingHandler.postDelayed(loadingRunnable!!, SHOW_LOADING_DELAY_MS)
    }

    private fun dismissLoadingDialog() {
        loadingRunnable?.let { loadingHandler.removeCallbacks(it) }
        loadingRunnable = null
        try {
            adLoadingDialog?.dismiss()
        } catch (_: Exception) {
        }
        adLoadingDialog = null
    }

    private fun isActivityValid(activity: Activity): Boolean {
        if (activity.isFinishing) return false
        return !activity.isDestroyed
    }

    override fun setLoadCondition(condition: LoadCondition?) {
        additionalLoadCondition = condition
    }

    override fun setShowCondition(condition: ShowCondition?) {
        additionalShowCondition = condition
    }

    private fun setFullScreenContentCallback(
        interstitialAd: InterstitialAd,
        onAdComplete: () -> Unit
    ) {
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                AdsManagerState.onShowFullScreen()
                super.onAdShowedFullScreenContent()
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                dismissFullScreenLastTime = System.currentTimeMillis()
                AdsManagerState.onDismissFullScreen()
                onAdComplete()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                super.onAdFailedToShowFullScreenContent(error)
                AdsManagerState.onDismissFullScreen()
                onAdComplete()
            }
        }
    }
}