package com.wavez.themewallpaper.ui.launch

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.ads.admob.GoogleMobileAdsConsentManager
import com.ads.admob.MMonetize
import com.ads.admob.interstitial_ads.InterSplashManager
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.ads.AdsId
import com.wavez.themewallpaper.ads.native_ad.NativeAdPlacement
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.databinding.ActivityLaunchBinding
import com.wavez.themewallpaper.ui.home.MainActivity
import com.wavez.themewallpaper.ui.language.activity.OnboardingLanguageActivity
import com.wavez.themewallpaper.ui.premium.PremiumActivity
import com.wavez.themewallpaper.ui.utils.AppEvent
import com.wavez.themewallpaper.ui.utils.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class LaunchActivity : BaseActivity<ActivityLaunchBinding>() {

    override fun bindingView() = ActivityLaunchBinding.inflate(layoutInflater)

    private val splashAd by lazy {
        InterSplashManager(
            context = this,
            adUnitIds = AdsId.getListInterstitialAdsSplash(),
            shouldShow = { sharedPref.shouldShowInterSplash },
        )
    }

    private val viewModel by viewModels<LaunchViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.bgSplash.setImageResource(R.drawable.bg_splash)
        binding.ivIcon.setImageResource(R.mipmap.ic_launcher)
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        logSplashViewEvent()
        viewModel.preloadData()
        getInstanceGdpr()
    }

    private fun logSplashViewEvent() {
        val isReopen = sharedPref.hasLoggedSplashView
        FirebaseControlLogEvent.logEvent(
            if (isReopen) AppEvent.SPLASH_REOPEN_VIEW else AppEvent.SPLASH_1ST_VIEW
        )
        if (!isReopen) sharedPref.hasLoggedSplashView = true
    }

    override fun onVipChangedState(isPremium: Boolean) {
        super.onVipChangedState(isPremium)
        binding.tvAds.isVisible = !isPremium
    }

    private fun getInstanceGdpr() {
        startLoading()
        GoogleMobileAdsConsentManager.getInstance(this).gatherConsent(this) {
            loadBannerSplash()
            initNativeAd()
            splashAd.load()
            showInterSplash()
        }
    }

    private fun showInterSplash() {
        lifecycleScope.launch {
            splashAd.showOrTimeout(this@LaunchActivity) {
                goToNextActivity()
            }
        }
    }

    private fun initNativeAd() {
        viewModel.initNativeAd().forEach { (position, pair) ->
            val (adIds, shouldLoad) = pair
            if (shouldLoad) MMonetize.nativeAdRepository.setNativeAdIds(position.name, adIds)
        }
        if (sharedPref.isNeedShowLanguage) {
            loadNativeWithPosition(NativeAdPlacement.LANGUAGE)
        }
    }

    private fun startLoading() {
        lifecycleScope.launch {
            var progress = 0
            while (progress < 90) {
                delay(50)
                progress = (progress + (1..3).random()).coerceAtMost(90)
                binding.progressSplash.setProgress(progress, true)
            }
        }
    }

    private fun goToNextActivity() {
        when {
            sharedPref.isNeedShowLanguage -> startActivity(OnboardingLanguageActivity.newIntent(this))
            sharedPref.isNeedGotoWalkthrough -> { /* startActivity(WalkthroughActivity.newIntent(this)) */
            }

            intent.getStringExtra("shortcut_id") == "static" -> { /* startActivity(UninstallActivity.getIntent(this)) */
            }

            else -> startActivity(
                if (!sharedPref.isVip) PremiumActivity.newIntent(this, false)
                else MainActivity.newIntent(this)
            )
        }
        finish()
    }

    private fun loadBannerSplash() {
        binding.bannerAds.registerBanner(
            this,
            adId = AdsId.getBannerAdsSplash(),
            shouldLoad = sharedPref.shouldBannerSplash ,
        )
    }
}
