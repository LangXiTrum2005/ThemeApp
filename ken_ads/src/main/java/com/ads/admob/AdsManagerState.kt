package com.ads.admob

import java.util.concurrent.atomic.AtomicBoolean

object AdsManagerState {
    private val isBackScreen = AtomicBoolean(false)
    private val isOpenAdDisabled = AtomicBoolean(false)

    var isAdShowFullScreen = false
        private set

    fun onBackScreen() {
        isBackScreen.set(true)
    }

    fun consumeBackScreen(): Boolean {
        return isBackScreen.getAndSet(false)
    }

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