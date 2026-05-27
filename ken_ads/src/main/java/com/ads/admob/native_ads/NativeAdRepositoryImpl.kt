package com.ads.admob.native_ads

import android.content.Context
import android.util.Log
import com.ads.admob.AdRevenueTracker
import com.ads.admob.LoadCondition
import com.ads.admob.ShowCondition
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
    private val context: Context
) : NativeAdRepository {

    companion object {
        const val TAG = "Log_Native_High_floor"
    }

    private var additionalLoadCondition: LoadCondition? = null
    private var additionalShowCondition: ShowCondition? = null
    private val preloadCountMap = ConcurrentHashMap<String, Int>()
    private val adUnitMap = ConcurrentHashMap<String, List<String>>()
    private val adIdIndexMap = ConcurrentHashMap<String, AtomicInteger>()
    private val nativeAdsMap = ConcurrentHashMap<String, MutableList<NativeAd>>()
    private val nativeAdLoadStateMap =
        ConcurrentHashMap<String, MutableStateFlow<NativeAdLoadState>>()
    private val isLoadingMap = ConcurrentHashMap<String, AtomicBoolean>()

    override var preloadCount: Int = 1
        set(value) {
//            require(value > 0) { "preloadCount must be positive" }
            field = value
        }

    private val perLoadAfterClickMap = ConcurrentHashMap<String, Boolean>()

    fun setPerLoadAfterClick(adPlacement: String, enabled: Boolean) {
        perLoadAfterClickMap[adPlacement] = enabled
    }

    private fun shouldPerLoadAfterClick(adPlacement: String): Boolean {
        return perLoadAfterClickMap[adPlacement] ?: false
    }

    /**
     * Set preloadCount cho từng placement
     */
    override fun setPreloadCount(adPlacement: String, count: Int) {
//        require(count > 0) { "preloadCount must be positive" }
        preloadCountMap[adPlacement] = count
        Log.d(TAG, "Preload count set for [$adPlacement]: $count")
    }

    private fun getPreloadCount(adPlacement: String): Int {
        return preloadCountMap[adPlacement] ?: 1 // default = 1
    }

    /**
     * Ensures a [MutableStateFlow] for the given [adPlacement] exists, initializing with [NativeAdLoadState.Idle].
     */
    private fun ensureStateFlow(adPlacement: String): MutableStateFlow<NativeAdLoadState> {
        return nativeAdLoadStateMap.computeIfAbsent(adPlacement) {
            MutableStateFlow(
                NativeAdLoadState.Idle
            )
        }
    }

    /**
     * Checks if an ad is currently loading for the given [adPlacement].
     */
    private fun isLoading(adPlacement: String): Boolean =
        isLoadingMap.computeIfAbsent(adPlacement) { AtomicBoolean(false) }.get()

    /**
     * Sets the loading state for the given [adPlacement].
     */
    private fun setLoading(adPlacement: String, loading: Boolean) {
        isLoadingMap.computeIfAbsent(adPlacement) { AtomicBoolean(false) }.set(loading)
    }

    /**
     * Checks if an ad should be loaded for the given [adPlacement].
     */
    private fun shouldLoad(adPlacement: String): Boolean {
        if (isLoading(adPlacement)) {
            Log.d(TAG, "⏳ [$adPlacement] Skipping load: already loading.")
            return false
        }
        Log.d(TAG, "shouldLoad: $adPlacement --- ${nativeAdsMap[adPlacement]}")
        val currentAds = nativeAdsMap[adPlacement]?.size ?: 0
        val target = getPreloadCount(adPlacement)
        if (currentAds >= target) {
            Log.d(TAG, "✅ [$adPlacement] Preload limit reached ($currentAds/$target).")
            return false
        }
        if (additionalLoadCondition?.shouldLoad() == false) {
            Log.d(TAG, "Skip load: Blocked by additionalLoadCondition for $adPlacement")
            return false
        }
        return true
    }


    /**
     * Loads a native ad for the specified [adPlacement] with retry logic for ad unit IDs.
     */
    override fun loadAd(adPlacement: String, perLoadAfterClick: Boolean) {
        Log.d(TAG, "loadAd START: $adPlacement - ${System.currentTimeMillis()}")

        val adUnitIds = adUnitMap[adPlacement]
        if (adUnitIds.isNullOrEmpty()) {
            onFailed(adPlacement, "No ad unit IDs defined for $adPlacement")
            Log.w(TAG, "⚠️ [$adPlacement] No ad unit IDs defined.")
            return
        }

        if (!shouldLoad(adPlacement)) {
            onFailed(adPlacement, "Skip load: Blocked by additionalLoadCondition for $adPlacement")
            return
        }
        setPerLoadAfterClick(adPlacement, perLoadAfterClick)

        val currentIndex = adIdIndexMap[adPlacement]?.get() ?: 0
        val nativeAdId = getCurrentAdUnitId(adPlacement)
        Log.d(
            TAG,
            "🚀 [$adPlacement] Start loading → ID[$currentIndex/${adUnitIds.lastIndex}]: $nativeAdId"
        )

        onLoading(adPlacement)

        val adLoader = AdLoader.Builder(context, nativeAdId).forNativeAd { ad ->

            AdRevenueTracker.trackAdRevenue(ad)
            Log.d(TAG, "✅ [$adPlacement] SUCCESS at index=$currentIndex → $nativeAdId")
            adIdIndexMap[adPlacement]?.set(0)
            val ads = nativeAdsMap.computeIfAbsent(adPlacement) { mutableListOf() }
            synchronized(ads) { ads.add(ad) }
            onLoaded(adPlacement)
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.w(TAG, "❌ [$adPlacement] FAILED (index=$currentIndex) → ${p0.message}")
                val ids = adUnitMap[adPlacement] ?: return
                val currentIndex = adIdIndexMap[adPlacement]?.get() ?: 0
                val indexRef = adIdIndexMap[adPlacement] ?: return
                if (currentIndex >= ids.lastIndex) {
                    indexRef.set(0)
                    onFailed(adPlacement, p0.message)
                    Log.e(TAG, "🛑 [$adPlacement] All ad IDs exhausted, stopping retries.")
                    return
                }
                indexRef.incrementAndGet()
                onFailed(adPlacement, p0.message)
                loadAd(adPlacement, shouldPerLoadAfterClick(adPlacement))
                Log.d(
                    TAG,
                    "🔁 [$adPlacement] Retrying with next ID : index=$indexRef for $adPlacement"
                )
            }

            override fun onAdClicked() {
                super.onAdClicked()
                if (shouldPerLoadAfterClick(adPlacement)) {
                    ensureStateFlow(adPlacement).value = NativeAdLoadState.AdClicked
                    loadAd(adPlacement, perLoadAfterClick = true)
                }
                Log.d(TAG, "🖱️ [$adPlacement] Ad clicked.")
            }

        }).withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    /**
     * Sets the state to loaded for the given [adPlacement].
     */
    private fun onLoaded(adPlacement: String) {
        setLoading(adPlacement, false)
        ensureStateFlow(adPlacement).value = NativeAdLoadState.Loaded
    }


    /**
     * Sets the state to loading for the given [adPlacement].
     */
    private fun onLoading(adPlacement: String) {
        setLoading(adPlacement, true)
        ensureStateFlow(adPlacement).value = NativeAdLoadState.Loading
    }

    /**
     * Sets the state to failed for the given [adPlacement] with an error message.
     */
    private fun onFailed(adPlacement: String, errorMessage: String) {
        setLoading(adPlacement, false)
        ensureStateFlow(adPlacement).value = NativeAdLoadState.Failed(errorMessage)
    }

    /**
     * Sets the list of ad unit IDs for a given [adPlacement].
     */
    override fun setNativeAdIds(adPlacement: String, ids: List<String>) {
        require(ids.isNotEmpty()) { "Ad unit IDs list cannot be empty" }
        adUnitMap[adPlacement] = ids
        adIdIndexMap[adPlacement] = AtomicInteger(0)
        Log.d(TAG, "🧩 [$adPlacement] Registered Ad IDs: ${ids.joinToString()}")
    }

    /**
     * Retrieves the current ad unit ID for the given [adPlacement].
     */
    private fun getCurrentAdUnitId(adPlacement: String): String {
        val ids =
            adUnitMap[adPlacement]
                ?: throw kotlin.IllegalStateException("Ad unit IDs not set for $adPlacement")
        val index = adIdIndexMap[adPlacement]?.get() ?: 0
        return ids[index.coerceIn(0, ids.lastIndex)]
    }

    /**
     * Retrieves and removes the first loaded native ad for the given [adPlacement].
     */
    override fun getLoadedNativeAd(adPlacement: String): NativeAd? {
        val canShow = additionalShowCondition?.shouldShow() ?: true
        if (!canShow) return null
        val adList = nativeAdsMap[adPlacement] ?: return null
        return synchronized(adList) {
            val ad = adList.firstOrNull() ?: return null
            adList.remove(ad)
            ad
        }
    }

    /**
     * Clears all loaded ads and resets state for the given [adPlacement].
     */
    override fun clearAds(adPlacement: String) {
        val adList = nativeAdsMap.remove(adPlacement) ?: return
        synchronized(adList) {
            adList.forEach { ad ->
                ad.destroy()
            }
            adList.clear()
        }
        adIdIndexMap[adPlacement]?.set(0)
        isLoadingMap[adPlacement]?.set(false)
        ensureStateFlow(adPlacement).value = NativeAdLoadState.Idle
        Log.d(TAG, "🧹 [$adPlacement] Cleared all ads and reset state.")
    }

    /**
     * Clears all loaded ads and resets state for all adPlacements.
     */
    fun clearAllAds() {
        nativeAdsMap.keys.forEach { clearAds(it) }
        nativeAdsMap.clear()
        adUnitMap.clear()
        adIdIndexMap.clear()
        isLoadingMap.clear()
        nativeAdLoadStateMap.clear()
        Log.d(TAG, "🧨 Cleared ALL native ads and state.")
    }

    /**
     * Returns the [StateFlow] for tracking ad load state for the given [adPlacement].
     */
    override fun getNativeAdLoadFlow(adPlacement: String): StateFlow<NativeAdLoadState> {
        return ensureStateFlow(adPlacement).asStateFlow()
    }


    override fun setLoadCondition(condition: LoadCondition?) {
        additionalLoadCondition = condition
    }

    override fun setShowCondition(condition: ShowCondition?) {
        additionalShowCondition = condition
    }

}
