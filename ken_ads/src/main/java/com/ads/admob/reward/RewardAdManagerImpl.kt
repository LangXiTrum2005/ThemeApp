package com.ads.admob.reward

import android.app.Activity
import android.content.Context
import android.util.Log
import com.ads.admob.AdRevenueTracker
import com.ads.admob.GoogleMobileAdsConsentManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RewardAdManagerImpl(private val context: Context) : RewardAdManager {

    companion object {
        const val TAG = "Log_reward"
    }

    private var rewardedAd: RewardedAd? = null
    private val adLoadState = MutableStateFlow(RewardLoadState.Initial)
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)
    private var adUnitIds: List<String> = emptyList() // danh sách 2 ID
    private var currentAdIndex = 0
    private var isShowAdProgress = false

    override fun setAdUnitIds(ids: List<String>) {
        if (ids.isEmpty()) throw IllegalArgumentException("Ad unit ids cannot be empty")
        adUnitIds = ids
        currentAdIndex = 0
    }

    override fun loadRewardAd() {
        Log.d("Log_reward", "loadRewardAd:")

        if (adUnitIds.isEmpty()) throw Exception("Reward ad unit ids must be set")
        if (!googleMobileAdsConsentManager.canRequestAds) {
            adLoadState.tryEmit(RewardLoadState.Failed)
            return
        }
        if (rewardedAd != null) return
        if (adLoadState.value != RewardLoadState.Initial) return

        if (currentAdIndex >= adUnitIds.size) {
            currentAdIndex = 0
            return
        }

        val adUnitId = adUnitIds[currentAdIndex]

        adLoadState.tryEmit(RewardLoadState.Loading)
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("Log_reward", "onAdFailedToLoad: ${adError.message}")
                adLoadState.tryEmit(RewardLoadState.Failed)
                currentAdIndex++
                if (currentAdIndex < adUnitIds.size) {
                    loadRewardAd()
                }
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "onAdLoaded: Ad loaded successfully with unit id: $adUnitId")
                AdRevenueTracker.trackAdRevenue(ad)
                rewardedAd = ad
                adLoadState.tryEmit(RewardLoadState.Loaded)
            }
        })
    }

    override suspend fun show(activity: Activity, onStateChanged: (RewardAdShowState) -> Unit) {
        Log.d("sdfsfs", "show: $isShowAdProgress")
        if (isShowAdProgress) return
        isShowAdProgress = true
        withContext(Dispatchers.Main) {
            adLoadState.collect { state ->
                when (state) {
                    RewardLoadState.Initial -> {
                        Log.d(TAG, "show: Initial state, cannot show ad")
                        onStateChanged(RewardAdShowState.Initial)
                        Log.d(TAG, "show: Initial state, cannot show ad")
                        onStateChanged(RewardAdShowState.Initial)
                    }

                    RewardLoadState.Loading -> {
                        Log.d(TAG, "show: Ad is still loading")
                        onStateChanged(RewardAdShowState.Loading)
                    }

                    RewardLoadState.Loaded -> {
                        val rewardedAd =
                            rewardedAd ?: throw IllegalStateException("Reward ad is required")
                        show(rewardedAd, onStateChanged, activity)
                    }

                    RewardLoadState.Failed -> {
                        onStateChanged(RewardAdShowState.Failed("Reward failed to load"))
                        finishShowAdProgress()
                    }
                }
            }
        }
    }

    private fun CoroutineScope.show(
        rewardedAd: RewardedAd,
        onStateChanged: (RewardAdShowState) -> Unit,
        activity: Activity
    ) {
        var isEarned = false

        rewardedAd.fullScreenContentCallback =
            object : FullScreenContentCallback() {

            override fun onAdClicked() {
                onStateChanged(RewardAdShowState.Clicked)
            }

            override fun onAdDismissedFullScreenContent() {
                onStateChanged(RewardAdShowState.Completed(isEarned))
                finishShowAdProgress()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                onStateChanged(RewardAdShowState.Failed(adError.message))
                finishShowAdProgress()
            }

            override fun onAdImpression() {
                onStateChanged(RewardAdShowState.Impression)
            }

            override fun onAdShowedFullScreenContent() {
                onStateChanged(RewardAdShowState.Showing)
            }
        }

        rewardedAd.show(activity) {
            isEarned = true
            onStateChanged(RewardAdShowState.RewardEarned)
        }
    }

    private fun CoroutineScope.finishShowAdProgress() {
        cancel()
        isShowAdProgress = false
        resetRewardAd()
    }

    private fun resetRewardAd() {
        rewardedAd = null
        adLoadState.value = RewardLoadState.Initial
    }

}