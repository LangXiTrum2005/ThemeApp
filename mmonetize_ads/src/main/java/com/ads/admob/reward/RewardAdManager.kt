package com.ads.admob.reward

import android.app.Activity

interface RewardAdManager {

    fun setAdUnitIds(ids: List<String>)

    fun loadRewardAd()

    suspend fun show(activity: Activity, onStateChanged: (RewardAdShowState) -> Unit)

}