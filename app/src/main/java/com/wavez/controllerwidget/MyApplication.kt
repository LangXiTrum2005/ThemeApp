package com.wavez.controllerwidget

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ads.admob.AdsInitializer
import com.ads.admob.AdsManagerState
import com.ads.admob.open_ads.OpenAdManager
import com.google.android.gms.ads.AdActivity
import com.wavez.controllerwidget.ads.AppRemoteConfig
import com.wavez.controllerwidget.data.repository.PremiumRepository
import com.wavez.controllerwidget.data.repository.SharedPref
import com.wavez.controllerwidget.ui.language.activity.OnboardingLanguageActivity
import com.wavez.controllerwidget.ui.launch.LaunchActivity
import com.wavez.controllerwidget.ui.onboarding.WalkthroughActivity
import com.wavez.controllerwidget.ui.premium.PremiumActivity
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
    lateinit var openManager: OpenAdManager

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
        AdsInitializer.initializeAds(this) {}
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
        openManager.show(activity)
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