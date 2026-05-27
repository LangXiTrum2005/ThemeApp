package com.wavez.controllerwidget.ads.native_ad

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ads.admob.native_ads.NativeAdRepository
import com.ads.admob.native_ads.adapter.NativeAdLoadState
import com.ads.admob.native_ads.adapter.NativeAdPlaceholder
import com.ads.admob.native_ads.state.NativeAdState
import com.google.android.gms.ads.nativead.NativeAd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class NativeAdViewModel @Inject constructor(
    private val nativeAdRepository: NativeAdRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "NativeAdViewModel"
    }

    private val nativeAdCache = mutableMapOf<String, NativeAd?>()

    private val nativeAdMap = mutableMapOf<String, MutableLiveData<NativeAdState>>()

    private val observedPositions = mutableSetOf<String>()

    fun ensureObserved(position: NativeAdPlacement) {
        val key = position.name
        if (nativeAdMap[key] == null) {
            nativeAdMap[key] = MutableLiveData(NativeAdState.Loading)
        }
        if (observedPositions.add(key)) {
            observePositionFlow(key)
        }
    }

    private fun observePositionFlow(adPlacement: String) {
        nativeAdRepository.getNativeAdLoadFlow(adPlacement)
            .onEach { state ->
                when (state) {
                    NativeAdLoadState.Loaded -> {
                        val ad = nativeAdRepository.getLoadedNativeAd(adPlacement)
                        Log.d(TAG, "[$adPlacement] Loaded: $ad")
                        val cachedAd = nativeAdCache[adPlacement]
                        if (cachedAd != ad) {
                            clearAdOld(adPlacement)
                        }
                        nativeAdCache[adPlacement] = ad
                        nativeAdMap[adPlacement]?.value = NativeAdState.Loaded(ad)
                    }

                    is NativeAdLoadState.Failed -> {
                        Log.d(TAG, "[$adPlacement] Failed")
                        nativeAdMap[adPlacement]?.value = NativeAdState.Failed()
                    }

                    is NativeAdLoadState.Loading -> {
                        Log.d(TAG, "[$adPlacement] Loading")
                        nativeAdMap[adPlacement]?.value = NativeAdState.Loading
                    }

                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    fun setNativeAdIds(position: NativeAdPlacement, adIds: List<String>) {
        nativeAdRepository.setNativeAdIds(position.name, adIds)
    }

    fun loadAd(position: NativeAdPlacement, perLoadAfterClick: Boolean = false) {
        nativeAdRepository.loadAd(position.name, perLoadAfterClick)
    }

    fun getNativeFlowByPosition(position: NativeAdPlacement): LiveData<NativeAdState> {
        val key = position.name
        return nativeAdMap.getOrPut(key) { MutableLiveData(NativeAdState.Loading) }
    }

    fun getNativeAdPlaceholder(nativeAdPosition: NativeAdPlacement): NativeAdPlaceholder {
        return NativeAdPlaceholder(
            nativeAdRepository,
            nativeAdPosition.name,
            nativeAdCache[nativeAdPosition.name],
        )
    }

    /**
     * Destroy ad cũ trong cache (khi có ad mới load về thay thế).
     */
    fun clearAdOld(adPlacement: String) {
        nativeAdCache[adPlacement]?.destroy()
        nativeAdCache[adPlacement] = null
    }

    /**
     * FIX: Trước đây thiếu destroy() cho currentAd → memory leak.
     * Nay destroy trước khi xóa reference.
     */
    fun clearNativeWithPosition(adPlacement: String) {
        nativeAdCache[adPlacement]?.destroy()       // ✅ destroy trước
        nativeAdCache[adPlacement] = null
        nativeAdRepository.clearAds(adPlacement)
        nativeAdMap[adPlacement]?.postValue(null)
    }

    override fun onCleared() {
        super.onCleared()
        // Destroy tất cả ad còn trong cache khi ViewModel bị clear
        nativeAdCache.values.forEach { it?.destroy() }
        nativeAdCache.clear()
        observedPositions.clear()
    }
}