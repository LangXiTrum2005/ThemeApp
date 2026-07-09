package com.wavez.themewallpaper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ads.admob.AdsManagerState
import com.ads.admob.MMonetize
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import com.google.android.gms.ads.AdActivity
import com.wavez.themewallpaper.ads.AppRemoteConfig
import com.wavez.themewallpaper.data.repository.PremiumRepository
import com.wavez.themewallpaper.data.repository.SharedPref
import com.wavez.themewallpaper.ui.language.activity.OnboardingLanguageActivity
import com.wavez.themewallpaper.ui.launch.LaunchActivity
import com.wavez.themewallpaper.ui.onboarding.WalkthroughActivity
import com.wavez.themewallpaper.ui.premium.PremiumActivity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var app: MyApplication

        fun instance(): MyApplication {
            return app
        }

        fun appContext(): Context = instance().applicationContext
    }

    @Inject
    lateinit var premiumRepository: PremiumRepository

    @Inject
    lateinit var sharedPref: SharedPref

    @Inject
    lateinit var appRemoteConfig: AppRemoteConfig

    private var currentActivity: Activity? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val excludedActivities = listOf(
        LaunchActivity::class.java,
        AdActivity::class.java,
        OnboardingLanguageActivity::class.java,
        WalkthroughActivity::class.java,
        PremiumActivity::class.java,
    )

    override fun onCreate() {
        super<Application>.onCreate()
        app = this
        appRemoteConfig.fetchRemoteConfig()
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        premiumRepository.startConnection()
        initPangleSdk()
        MMonetize.initializeAds(this) {}
    }

    private fun initPangleSdk() {
        Log.d("LOG_TAG", "initializeMobileAdsSdk: "+packageName)
        val pangleConfig = PAGConfig.Builder()
            .appId("8845554")
            .appIcon(R.mipmap.ic_launcher)
            .supportMultiProcess(false)
            .setPackageName(BuildConfig.APPLICATION_ID)
            .debugLog(BuildConfig.DEBUG)
            .build()

        PAGSdk.init(applicationContext, pangleConfig, object : PAGSdk.PAGInitCallback {
            override fun success() {
                Log.i("LOG_TAG", "Pangle init success")
            }

            override fun fail(code: Int, msg: String?) {
                Log.e("LOG_TAG", "Pangle init fail: code=$code msg=$msg")
            }
        })
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let { activity ->
            if (sharedPref.isVip || AdsManagerState.consumeDisableOpenAd() || excludedActivities.any {
                    it.isInstance(
                        activity
                    )
                }) {
                return
            }
            coroutineScope.launch {
                try {
                    showOpenAd()
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun showOpenAd() {
        val activity = currentActivity ?: return
        MMonetize.openAdManager.show(activity)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        coroutineScope.cancel()
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStarted(p0: Activity) {
        this.currentActivity = p0
    }

    override fun onActivityStopped(p0: Activity) {
    }


}