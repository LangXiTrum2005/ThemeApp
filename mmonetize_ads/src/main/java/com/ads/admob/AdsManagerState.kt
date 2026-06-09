package com.ads.admob

import java.util.concurrent.atomic.AtomicBoolean

object AdsManagerState {
    private val isOpenAdDisabled = AtomicBoolean(false)

    var isAdShowFullScreen = false
        private set

    fun onShowFullScreen() {
        isAdShowFullScreen = true
    }

    fun onDismissFullScreen() {
        isAdShowFullScreen = false
    }

    fun disableOpenAdOnce() {
        isOpenAdDisabled.set(true)
    }

    fun consumeDisableOpenAd(): Boolean {
        return isOpenAdDisabled.getAndSet(false)
    }

}