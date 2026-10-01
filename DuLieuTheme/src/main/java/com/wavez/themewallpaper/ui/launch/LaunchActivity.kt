package com.wavez.themewallpaper.ui.launch

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.databinding.ActivityLaunchBinding
import com.wavez.themewallpaper.ui.home.MainActivity
import com.wavez.themewallpaper.ui.language.activity.OnboardingLanguageActivity
import com.wavez.themewallpaper.ui.setting.dialog.RatePromptManager
import com.wavez.themewallpaper.ui.utils.AppEvent
import com.wavez.themewallpaper.ui.utils.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class LaunchActivity : BaseActivity<ActivityLaunchBinding>() {

    override fun bindingView() = ActivityLaunchBinding.inflate(layoutInflater)

    private val viewModel by viewModels<LaunchViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.bgSplash.setImageResource(R.drawable.bg_splash)
        binding.ivIcon.setImageResource(R.mipmap.ic_launcher)
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        prepareRatePrompt(savedInstanceState)
        logSplashViewEvent()
        viewModel.preloadData()
        startLoading()
    }

    private fun prepareRatePrompt(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            RatePromptManager.prepareMainPromptAfterSplash(sharedPref)
        }
    }

    private fun logSplashViewEvent() {
        val isReopen = sharedPref.hasLoggedSplashView
        FirebaseControlLogEvent.logEvent(
            if (isReopen) AppEvent.SPLASH_REOPEN_VIEW else AppEvent.SPLASH_1ST_VIEW
        )
        if (!isReopen) sharedPref.hasLoggedSplashView = true
    }

    private fun startLoading() {
        lifecycleScope.launch {
            var progress = 0
            while (progress < 100) {
                delay(50)
                progress = (progress + (1..3).random()).coerceAtMost(100)
                binding.progressSplash.setProgress(progress, true)
            }
            goToNextActivity()
        }
    }

    private fun goToNextActivity() {
        when {
            sharedPref.isNeedShowLanguage -> startActivity(OnboardingLanguageActivity.newIntent(this))
            sharedPref.isNeedGotoWalkthrough -> { /* startActivity(WalkthroughActivity.newIntent(this)) */
            }

            intent.getStringExtra("shortcut_id") == "static" -> { /* startActivity(UninstallActivity.getIntent(this)) */
            }

            else -> startActivity(MainActivity.newIntent(this))
        }
        finish()
    }

}
