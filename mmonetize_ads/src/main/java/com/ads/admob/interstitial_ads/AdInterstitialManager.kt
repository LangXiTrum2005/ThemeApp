package com.ads.admob.interstitial_ads

import android.app.Activity
import android.view.View
import com.ads.admob.LoadCondition
import com.ads.admob.ShowCondition

interface AdInterstitialManager {

    val dismissFullScreenLastTime: Long

    fun isAdReady(): Boolean

    fun isAdEligibleToShow(): Boolean

    fun loadAd()

    fun setAdUnitIds(ids: List<String>)

    fun showAdFullScreen(
        activity: Activity,
        showLoading: Boolean = true,
        loadingView: View? = null,
        preloadOnShow: Boolean = true,
        onAdComplete: () -> Unit
    )

    fun forceShowAdFullScreen(
        activity: Activity,
        showLoading: Boolean = true,
        loadingView: View? = null,
        preloadOnShow: Boolean = true,
        onAdComplete: () -> Unit
    )

    fun setLoadCondition(condition: LoadCondition?)

    fun setShowCondition(condition: ShowCondition?)
}