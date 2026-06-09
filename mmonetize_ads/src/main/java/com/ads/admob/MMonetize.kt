package com.ads.admob

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.ads.admob.interstitial_ads.AdInterstitialManager
import com.ads.admob.native_ads.NativeAdRepository
import com.ads.admob.open_ads.OpenAdManager
import com.ads.admob.reward.RewardAdManager
import com.google.android.gms.ads.MobileAds
import com.lib.mobileads.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.get

/**
 * Convenience accessors for ad singletons managed by Koin.
 * Koin is started by [AdsInitializer.initializeAds] — call that once in
 * Application.onCreate() before any ad functionality is used.
 */
object MMonetize {
    val nativeAdRepository: NativeAdRepository get() = get(NativeAdRepository::class.java)
    val interstitialManager: AdInterstitialManager get() = get(AdInterstitialManager::class.java)
    val openAdManager: OpenAdManager get() = get(OpenAdManager::class.java)
    val rewardAdManager: RewardAdManager get() = get(RewardAdManager::class.java)

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            resumeAdjustSdk()
        }

        override fun onActivityPaused(activity: Activity) {
            pauseAdjustSdk()
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }
    }

    fun initializeAds(application: Application, onInitialized: () -> Unit) {
        // Start Koin with the lib's DI module — creates all ad singletons internally.
        startKoin {
            androidContext(application)
            modules(kenAdsModule())
        }
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        scope.launch {
            try {
                MobileAds.initialize(application) {
                    onInitialized()
                }
                withContext(Dispatchers.IO) {
                    initializeAdjustSdk(application)
                }
            } catch (e: Exception) {
                onInitialized()
            }
        }
    }

    private fun initializeAdjustSdk(context: Context) {
        val appToken = "avatowvhwnwg"
        val environment = if (BuildConfig.DEBUG) {
            AdjustConfig.ENVIRONMENT_SANDBOX
        } else {
            AdjustConfig.ENVIRONMENT_PRODUCTION
        }

        val config = AdjustConfig(context, appToken, environment).apply {
            setLogLevel(LogLevel.WARN)
        }

        Adjust.initSdk(config)
    }

    private fun resumeAdjustSdk() {
        Adjust.onResume()
    }

    private fun pauseAdjustSdk() {
        Adjust.onPause()
    }
}
