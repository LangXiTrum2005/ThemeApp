package com.wavez.controllerwidget.ui.launch

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.ads.admob.GoogleMobileAdsConsentManager
import com.ads.admob.interstitial_ads.AdInterstitialManagerImpl
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.ads.AdsId
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.ads.native_ad.NativeAdViewModel
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.databinding.ActivityLaunchBinding
import com.wavez.controllerwidget.ui.home.MainActivity
import com.wavez.controllerwidget.ui.language.activity.OnboardingLanguageActivity
import com.wavez.controllerwidget.ui.premium.PremiumActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class LaunchActivity : BaseActivity<ActivityLaunchBinding>() {
    override fun bindingView(): ActivityLaunchBinding {
        return ActivityLaunchBinding.inflate(layoutInflater)
    }

    companion object {
        private const val DELAY_TO_CHECK_AD = 100L

        private const val MIN_DELAY_TIME = 4000L

        private const val MAX_DELAY_TIME = 12000L
    }


    private val adInterSplash by lazy {
        AdInterstitialManagerImpl(this).apply {
            setAdUnitIds(AdsId.getListInterstitialAdsSplash())
        }
    }

    private var launchStartedTime = 0L

    private val viewModel by viewModels<LaunchViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.bgSplash.setImageResource(R.drawable.bg_splash)
        binding.ivIcon.setImageResource(R.mipmap.ic_launcher)
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        viewModel.preloadData()
        getInstanceGdpr()
    }

    private fun getInstanceGdpr() {
        Log.d("Launch", "getInstanceGdpr: ${sharedPref.shouldShowInterSplash}")

        if (sharedPref.isVip) {
            Log.d("Launch", "getInstanceGdpr: ")
            binding.bannerAds.goneBannerView()
            lifecycleScope.launch {
                delay(MIN_DELAY_TIME)
                goToNextActivity()
            }
            return
        }
        startLoading()
        val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)
        googleMobileAdsConsentManager.gatherConsent(this) {
            launchStartedTime = System.currentTimeMillis()
            lifecycleScope.launch {
                launch {
                    loadBannerSplash()
                }
                launch {
                    loadAllAds()
                }
            }
            showInterstitialIfAvailable()
        }
    }

    private fun loadAllAds() {
        initNativeAd()
        if (sharedPref.shouldShowInterSplash) {
            adInterSplash.loadAd()
        }
    }

    private fun initNativeAd() {
        viewModel.initNativeAd().forEach { (position, pair) ->
            val (adIds, shouldLoad) = pair
            if (shouldLoad) {
                nativeAdViewModel.setNativeAdIds(position, adIds)
            }
        }
        if (sharedPref.isNeedShowLanguage) {
            nativeAdViewModel.loadAd(NativeAdPlacement.LANGUAGE, true)
        }
    }

    private fun showInterstitialIfAvailable() {
        lifecycleScope.launch {
            delay(MIN_DELAY_TIME)
            if (!sharedPref.shouldShowInterSplash) {
                goToNextActivity()
                return@launch
            }
            while (isActive) {
                val elapsed = System.currentTimeMillis() - launchStartedTime
                if (adInterSplash.isAdReady() || elapsed >= MAX_DELAY_TIME) {
                    adInterSplash.showAdFullScreen(this@LaunchActivity, showLoading = false) {
                        goToNextActivity()
                    }
                    break
                }
                delay(DELAY_TO_CHECK_AD)
            }
        }
    }

    private fun startLoading() {
        val progressBar = binding.progressSplash

        lifecycleScope.launch {
            var progress = 0

            while (progress < 90) {
                delay(50)
                progress += (1..3).random()

                if (progress > 90) {
                    progress = 90
                }

                progressBar.setProgress(progress, true)
            }
        }
    }

    private fun goToNextActivity() {
        when {
            sharedPref.isNeedShowLanguage -> {
                startActivity(OnboardingLanguageActivity.newIntent(this))
            }

            sharedPref.isNeedGotoWalkthrough -> {
//                startActivity(WalkthroughActivity.newIntent(this))
            }

            intent.getStringExtra("shortcut_id") == "static" -> {
//                startActivity(UninstallActivity.getIntent(this))
            }

            else -> {
                if (!sharedPref.isVip) {
                    startActivity(PremiumActivity.newIntent(this, false))
                } else {
                    startActivity(MainActivity.newIntent(this))
                }
            }
        }
        finish()
    }

    private fun loadBannerSplash() {
        binding.bannerAds.registerBanner(
            this, adId = AdsId.getBannerAdsSplash(),
            shouldLoad = sharedPref.shouldBannerSplash && !sharedPref.isVip,
        )
    }
}