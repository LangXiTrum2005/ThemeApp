package com.ads.admob.native_ads

import android.content.Context
import android.util.Log
import com.ads.admob.AdRevenueTracker
import com.ads.admob.GoogleMobileAdsConsentManager
import com.ads.admob.LoadCondition
import com.ads.admob.native_ads.adapter.NativeAdLoadState
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class NativeAdRepositoryImpl(
    private val context: Context,
) : NativeAdRepository {

    companion object {
        private const val TAG = "Log_Native_High_floor"
    }

    private var loadCondition: LoadCondition? = null
    private var lastTimeLoadNative = 0L

    private val adUnitMap     = ConcurrentHashMap<String, List<String>>()
    private val adIdIndexMap  = ConcurrentHashMap<String, AtomicInteger>()
    private val adMap         = ConcurrentHashMap<String, NativeAd>()
    private val stateFlowMap  = ConcurrentHashMap<String, MutableStateFlow<NativeAdLoadState>>()
    private val loadingMap    = ConcurrentHashMap<String, AtomicBoolean>()
    private val preloadOnClickMap = ConcurrentHashMap<String, Boolean>()
    private val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(context)

    // ─── Public API ───────────────────────────────────────────────────────────

    override fun setNativeAdIds(adPlacement: String, ids: List<String>) {
        require(ids.isNotEmpty()) { "Ad unit IDs cannot be empty for $adPlacement" }
        adUnitMap[adPlacement] = ids
        adIdIndexMap[adPlacement] = AtomicInteger(0)
        Log.d(TAG, "🧩 [$adPlacement] Registered ${ids.size} ID(s).")
    }

    override fun setLoadCondition(condition: LoadCondition?) { loadCondition = condition }

    override fun setPreloadOnClick(adPlacement: String, enabled: Boolean) {
        preloadOnClickMap[adPlacement] = enabled
    }

    override fun loadAd(adPlacement: String, forceReload: Boolean) {
        val ids = adUnitMap[adPlacement]
        if (ids.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ [$adPlacement] No ad unit IDs registered.")
            emitState(adPlacement, NativeAdLoadState.Failed("No IDs for $adPlacement"))
            return
        }
        if (!canLoad(adPlacement, forceReload)) return

        val index = currentIndex(adPlacement)
        Log.d(TAG, "🚀 [$adPlacement] Loading ID[$index/${ids.lastIndex}]: ${ids[index]}")
        emitState(adPlacement, NativeAdLoadState.Loading)
        setLoading(adPlacement, true)
        lastTimeLoadNative = System.currentTimeMillis()

        AdLoader.Builder(context, ids[index])
            .forNativeAd { ad ->
                AdRevenueTracker.trackAdRevenue(ad)
                Log.d(TAG, "✅ [$adPlacement] Loaded (index=$index).")
                adIdIndexMap[adPlacement]?.set(0)
                adMap.put(adPlacement, ad)?.destroy()
                setLoading(adPlacement, false)
                emitState(adPlacement, NativeAdLoadState.Loaded(ad))
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "❌ [$adPlacement] Failed (index=$index): ${error.message}")
                    setLoading(adPlacement, false)
                    val indexRef = adIdIndexMap[adPlacement] ?: return
                    if (indexRef.get() < (adUnitMap[adPlacement]?.lastIndex ?: 0)) {
                        // Try next ID in the list.
                        indexRef.incrementAndGet()
                        emitState(adPlacement, NativeAdLoadState.Failed(error.message))
                        loadAd(adPlacement, forceReload)
                        Log.d(TAG, "🔁 [$adPlacement] Retrying with next ID.")
                    } else {
                        indexRef.set(0)
                        Log.e(TAG, "🛑 [$adPlacement] All IDs exhausted.")
                        emitState(adPlacement, NativeAdLoadState.Failed(error.message))
                    }
                }

                override fun onAdClicked() {
                    Log.d(TAG, "🖱️ [$adPlacement] Clicked — loading next.")
                    if (preloadOnClickMap[adPlacement] != false) {
                        emitState(adPlacement, NativeAdLoadState.AdClicked)
                        loadAd(adPlacement, forceReload = true)
                    }
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    override fun getAdStateFlow(adPlacement: String): StateFlow<NativeAdLoadState> =
        stateFlow(adPlacement).asStateFlow()

    override fun releaseAd(adPlacement: String) {
        adMap.remove(adPlacement)?.destroy()
        setLoading(adPlacement, false)
        emitState(adPlacement, NativeAdLoadState.Idle)
        Log.d(TAG, "🧹 [$adPlacement] Released.")
    }

    override fun clearAds(adPlacement: String) {
        adMap.remove(adPlacement)?.destroy()
        adIdIndexMap[adPlacement]?.set(0)
        setLoading(adPlacement, false)
        emitState(adPlacement, NativeAdLoadState.Idle)
        Log.d(TAG, "🧹 [$adPlacement] Cleared.")
    }

    // ─── Internals ────────────────────────────────────────────────────────────

    private fun canLoad(adPlacement: String, forceReload: Boolean): Boolean {
        if (!googleMobileAdsConsentManager.canRequestAds) return false
        if (isLoading(adPlacement)) {
            Log.d(TAG, "⏳ [$adPlacement] Skip: already loading.")
            return false
        }
        if (!forceReload && adMap.containsKey(adPlacement)) {
            Log.d(TAG, "✅ [$adPlacement] Skip: ad already ready.")
            return false
        }
        if (loadCondition?.shouldLoad() == false) {
            Log.d(TAG, "🚫 [$adPlacement] Skip: load condition blocked.")
            return false
        }
        return true
    }

    private fun currentIndex(adPlacement: String): Int =
        adIdIndexMap.computeIfAbsent(adPlacement) { AtomicInteger(0) }
            .get()
            .coerceIn(0, (adUnitMap[adPlacement]?.lastIndex ?: 0))

    private fun isLoading(adPlacement: String): Boolean =
        loadingMap.computeIfAbsent(adPlacement) { AtomicBoolean(false) }.get()

    private fun setLoading(adPlacement: String, value: Boolean) {
        loadingMap.computeIfAbsent(adPlacement) { AtomicBoolean(false) }.set(value)
    }

    private fun stateFlow(adPlacement: String): MutableStateFlow<NativeAdLoadState> =
        stateFlowMap.computeIfAbsent(adPlacement) { MutableStateFlow(NativeAdLoadState.Idle) }

    private fun emitState(adPlacement: String, state: NativeAdLoadState) {
        stateFlow(adPlacement).value = state
    }

    override fun getLastLoadTime(): Long {
        return lastTimeLoadNative
    }
}
