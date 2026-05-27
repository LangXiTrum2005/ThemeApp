package com.ads.admob.open_ads

import android.app.Activity
import com.ads.admob.LoadCondition
import com.ads.admob.ShowCondition
import kotlinx.coroutines.flow.StateFlow

interface OpenAdManager {

    val dismissFullScreenLastTime: Long

    val state: StateFlow<OpenAdState>

    fun isAdReady(): Boolean

    fun loadAd()

    fun setAdUnitId(adUnitId: String)

    fun show(activity: Activity, onContinueTask: (() -> Unit)? = null)

    fun setLoadCondition(condition: LoadCondition?)

    fun setShowCondition(condition: ShowCondition?)

}