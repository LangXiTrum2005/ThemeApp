package com.ads.admob

class AdLoadFailureDelay {

    companion object {
        private const val BASE_DELAY_TIME_MS = 5_000L
        private const val MAX_DELAY_TIME_MS = 60_000L
    }

    private var lastFailureTimestamp = 0L
    private var failureCount = 0

    fun shouldDelayRetry(): Boolean {
        if (failureCount == 0) return false
        val delayTime = (BASE_DELAY_TIME_MS * failureCount).coerceAtMost(MAX_DELAY_TIME_MS)
        return System.currentTimeMillis() < lastFailureTimestamp + delayTime
    }

    fun resetFailureCount() {
        failureCount = 0
    }

    fun recordFailedLoad() {
        failureCount += 1
        lastFailureTimestamp = System.currentTimeMillis()
    }

}