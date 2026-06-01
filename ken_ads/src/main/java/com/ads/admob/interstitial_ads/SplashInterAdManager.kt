package com.ads.admob.interstitial_ads

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SplashInterAdManager(
    context: Context,
    adUnitIds: List<String>,
    private val shouldShow: () -> Boolean,
    private val minDelayMs: Long = 4_000L,
    private val maxWaitMs: Long = 12_000L,
    private val checkIntervalMs: Long = 100L,
) {
    private val interstitial = AdInterstitialManagerImpl(context).apply {
        setAdUnitIds(adUnitIds)
    }

    fun loadInter() {
        if (!shouldShow()) return
        interstitial.loadAd()
    }

    fun showInterAd(activity: Activity, scope: CoroutineScope, onComplete: () -> Unit) {
        val startTime = System.currentTimeMillis()
        scope.launch {
            delay(minDelayMs)
            if (!shouldShow()) {
                onComplete()
                return@launch
            }
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                if (interstitial.isAdReady() || elapsed >= maxWaitMs) {
                    interstitial.showAdFullScreen(activity, showLoading = false) { onComplete() }
                    break
                }
                delay(checkIntervalMs)
            }
        }
    }
}
