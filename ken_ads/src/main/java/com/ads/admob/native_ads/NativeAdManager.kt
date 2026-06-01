package com.ads.admob.native_ads

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.ads.admob.AdsManagerState
import com.ads.admob.native_ads.adapter.NativeAdLoadState
import com.ads.admob.native_ads.adapter.NativeAdViewAd
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NativeAdManager(
    private val repository: NativeAdRepository,
    private val lifecycleOwner: LifecycleOwner,
    private val scope: CoroutineScope,
    private val isVip: () -> Boolean,
) {
    private val cache = mutableMapOf<String, NativeAd?>()
    private val autoReloadPlacements = mutableSetOf<String>()

    init {
        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && autoReloadPlacements.isNotEmpty()) {
                if (isVip() || !AdsManagerState.consumeBackScreen()) return@LifecycleEventObserver
                autoReloadPlacements.forEach { load(it) }
            }
        })
    }

    fun load(placement: String, reloadToClick: Boolean = false) {
        repository.loadAd(placement, reloadToClick)
    }

    fun setIds(placement: String, ids: List<String>) {
        repository.setNativeAdIds(placement, ids)
    }

    fun setup(
        placement: String,
        view: NativeAdViewAd,
        shouldShow: Boolean = true,
        autoLoad: Boolean = false,
        autoReloadOnStart: Boolean = false,
    ) {
        if (autoLoad) load(placement)
        if (autoReloadOnStart) autoReloadPlacements.add(placement)
        view.shouldShowAd = shouldShow && !isVip()
        scope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getNativeAdLoadFlow(placement).collectLatest { state ->
                    when (state) {
                        NativeAdLoadState.Loaded -> {
                            val cached = cache[placement]
                            if (cached != null) {
                                view.bind(cached)
                            } else {
                                val ad = repository.getLoadedNativeAd(placement)
                                cache[placement] = ad
                                if (ad != null) view.bind(ad) else view.hidePlaceholder()
                            }
                        }
                        NativeAdLoadState.Loading -> view.showPlaceholder()
                        is NativeAdLoadState.Failed -> view.hidePlaceholder()
                        else -> {}
                    }
                }
            }
        }
    }

    fun flowOf(placement: String): StateFlow<NativeAdLoadState> =
        repository.getNativeAdLoadFlow(placement)

    fun consumeAd(placement: String): NativeAd? =
        repository.getLoadedNativeAd(placement)

    fun release() {
        cache.values.forEach { it?.destroy() }
        cache.clear()
    }
}
