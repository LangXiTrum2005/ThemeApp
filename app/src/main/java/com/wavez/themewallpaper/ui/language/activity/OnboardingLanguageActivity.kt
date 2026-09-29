package com.wavez.themewallpaper.ui.language.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.extensions.gone
import com.wavez.themewallpaper.core.extensions.visible
import com.wavez.themewallpaper.core.utils.CustomToast
import com.wavez.themewallpaper.ui.onboarding.WalkthroughActivity
import com.wavez.themewallpaper.ui.utils.AppEvent
import com.wavez.themewallpaper.ui.utils.FirebaseControlLogEvent
import dagger.hilt.android.AndroidEntryPoint

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
            isSelectedLanguage = true
        }
        binding.btnNextLanguage.isSelected = true
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
