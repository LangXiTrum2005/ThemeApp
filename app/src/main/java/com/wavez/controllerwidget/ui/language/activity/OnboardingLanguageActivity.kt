package com.wavez.controllerwidget.ui.language.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.lifecycle.lifecycleScope
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.extensions.visible
import com.wavez.controllerwidget.core.utils.CustomToast
import com.wavez.controllerwidget.ui.onboarding.WalkthroughActivity
import com.wavez.controllerwidget.ui.utils.AppEvent
import com.wavez.controllerwidget.ui.utils.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingLanguageActivity : BaseLanguageActivity() {

    companion object {

        fun newIntent(context: Context) = Intent(context, OnboardingLanguageActivity::class.java)
    }

    private var isSelectedLanguage = false

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        FirebaseControlLogEvent.logEvent(AppEvent.LFO_VIEW)
        binding.btnNextLanguage.visible()
        binding.btnSave.gone()
        lifecycleScope.launch {
            loadNativeWithPosition(NativeAdPlacement.LANGUAGE_2, true)
        }
        initNativeLanguage1()
    }

    private fun loadAds() {
        lifecycleScope.launch {
            launch {
                nativeAdViewModel.loadAd(NativeAdPlacement.ONBOARDING1, perLoadAfterClick = true)
            }

            launch {
                nativeAdViewModel.loadAd(
                    NativeAdPlacement.ONBOARDING1_CLICK,
                    perLoadAfterClick = true,
                )
            }
        }
    }

    override fun onSaveLanguage() {
        super.onSaveLanguage()
        if (viewModel.selectedUiLanguage.value == null) {
            showToastFirstChoose()
        } else {
            FirebaseControlLogEvent.logEvent(AppEvent.LFO_CLICK_NEXT)
            startActivity(
                WalkthroughActivity.newIntent(this@OnboardingLanguageActivity),
            )
            finish()
        }
    }

    override fun updateStateLanguageSelected(isEnabled: Boolean) {
        if (!isSelectedLanguage) {
            FirebaseControlLogEvent.logEvent(AppEvent.LFO_CLICK_LANGUAGE)
            initNativeLanguage2()
            loadAds()
            isSelectedLanguage = true
        }
        binding.btnNextLanguage.isSelected = true
    }

    private fun initNativeLanguage1() {
        setupNative(
            NativeAdPlacement.LANGUAGE,
            binding.nativeLanguage1,
            sharedPref.shouldShowNativeLanguage1,
        )
    }

    private fun initNativeLanguage2() {
        binding.nativeLanguage2.visible()
        setupNative(
            NativeAdPlacement.LANGUAGE_2,
            binding.nativeLanguage2,
            sharedPref.shouldShowNativeLanguage2,
        )
    }

    private fun showToastFirstChoose() {
        val toast = CustomToast.makeText(
            this,
            contentId = R.string.please_select_your_language,
            bgDrawable = R.drawable.bg_toas,
            gravity = Gravity.TOP,
            textColor = R.color.white,
        )
        toast.show()
    }
}
