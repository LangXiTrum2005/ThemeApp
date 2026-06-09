package com.wavez.controllerwidget.ads.reward

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ads.admob.MMonetize
import com.ads.admob.reward.RewardAdManager
import com.ads.admob.reward.RewardAdShowState
import com.wavez.controllerwidget.ads.AdsId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardAdViewModel @Inject constructor(
) : ViewModel() {

    private val rewardAdManager: RewardAdManager = MMonetize.rewardAdManager

    private val _rewardAdState = MutableLiveData<RewardAdShowState>(RewardAdShowState.Initial)
    val rewardAdState: LiveData<RewardAdShowState> = _rewardAdState

    var reward: Any? = null
        private set

    fun loadAdAndShow(adsId: List<String> = AdsId.getRewardedSelectAds(), activity: Activity, reward: Any? = null) {
        this.reward = reward
        rewardAdManager.setAdUnitIds(adsId)
        rewardAdManager.loadRewardAd()
        showReward(activity)
    }

    private fun showReward(activity: Activity) {
        viewModelScope.launch {
            rewardAdManager.show(activity) { state ->
                _rewardAdState.value = state
            }
        }
    }

    fun onRewardAdCompleted() {
        reward = null
        _rewardAdState.value = RewardAdShowState.Initial
    }

}