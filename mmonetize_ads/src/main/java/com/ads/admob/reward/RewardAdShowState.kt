package com.ads.admob.reward

sealed class RewardAdShowState {
    object Initial : RewardAdShowState()
    object Loading : RewardAdShowState()
    object Showing : RewardAdShowState()
    object Clicked : RewardAdShowState()
    object Impression : RewardAdShowState()
    object RewardEarned : RewardAdShowState()
    data class Completed(val isRewarded: Boolean) : RewardAdShowState()
    data class Failed(val error: String) : RewardAdShowState()
}