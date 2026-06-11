package com.ads.admob.interstitial_ads

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Manages the splash-screen interstitial ad lifecycle.
 *
 * Responsibilities:
 * - Owns [AdInterstitialManagerImpl] — caller never touches it directly.
 * - Tracks elapsed time from [load] so the show window is measured correctly.
 * - [showOrTimeout] is a suspend function that guarantees [onComplete] is
 *   called exactly once: either after the ad dismisses or when the wait window
 *   expires — whichever comes first.
 *
 * Typical usage:
 * ```
 * // After GDPR consent:
 * splashManager.load()
 *
 * // In lifecycleScope:
 * splashManager.showOrTimeout(activity) { goToNextScreen() }
 * ```
 *
 * @param minDelayMs   Minimum splash duration regardless of ad state (default 4 s).
 * @param maxWaitMs    Maximum time to wait for the ad to load before giving up (default 12 s).
 * @param pollMs       How often to check ad readiness while waiting (default 100 ms).
 */
class InterSplashManager(
    context: Context,
    adUnitIds: List<String>,
    private val shouldShow: () -> Boolean,
    private val minDelayMs: Long = 4_000L,
    private val maxWaitMs: Long  = 12_000L,
    private val pollMs: Long     = 100L,
) {
    private val manager = AdInterstitialManagerImpl(context).apply {
        setAdUnitIds(adUnitIds)
    }

    private var loadStartedAt = 0L

    /**
     * Starts loading the ad. Must be called after GDPR consent is obtained.
     * No-op if [shouldShow] returns false.
     */
    fun load() {
        if (!shouldShow()) return
        loadStartedAt = System.currentTimeMillis()
        manager.loadAd()
    }

    /**
     * Waits [minDelayMs] (minimum splash duration), then:
     * - If [shouldShow] is false → calls [onComplete] immediately.
     * - If ad is ready → shows it, calls [onComplete] on dismiss.
     * - Keeps polling every [pollMs] until ad is ready or [maxWaitMs] elapses
     *   (measured from [load]), then shows whatever is available.
     *
     * [onComplete] is guaranteed to be called exactly once.
     * The coroutine is cancellation-safe: if the scope is cancelled the loop exits cleanly.
     */
    suspend fun showOrTimeout(activity: Activity, onComplete: () -> Unit) {
        delay(minDelayMs)

        if (!shouldShow()) {
            onComplete()
            return
        }

        while (currentCoroutineContext().isActive) {
            val elapsed = System.currentTimeMillis() - loadStartedAt
            if (manager.isAdReady() || elapsed >= maxWaitMs) {
                manager.showAdFullScreen(activity, showLoading = false, preloadOnShow = false) { onComplete() }
                return
            }
            delay(pollMs)
        }
    }
}
