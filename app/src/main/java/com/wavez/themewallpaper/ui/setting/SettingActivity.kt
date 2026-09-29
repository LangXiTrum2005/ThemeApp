package com.wavez.themewallpaper.ui.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.core.extensions.openUrl
import com.wavez.themewallpaper.databinding.ActivitySettingBinding
import com.wavez.themewallpaper.ui.language.activity.SettingLanguageActivity
import com.wavez.themewallpaper.ui.setting.dialog.RateDialogFragment
import com.wavez.themewallpaper.ui.setting.feedback.FeedbackActivity
import com.wavez.themewallpaper.ui.utils.PRIVACY_POLICY_URL
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    override fun bindingView(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SettingActivity::class.java)
        }
    }


    private val languageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            window.setWindowAnimations(0)
            recreate()
        }
    }


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    override fun initListener() {
        super.initListener()

        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }

        binding.btnPrivacy.setOnClickListener {
            openUrl(PRIVACY_POLICY_URL)
        }

        binding.btnFeedback.setOnClickListener {
            startActivity(FeedbackActivity.newIntent(this))
        }

        binding.btnRate.setOnClickListener {
            RateDialogFragment.newInstance(
                preSelectedStars = if (sharedPref.isRate) sharedPref.ratedStars else 0
            )
                .show(supportFragmentManager, RateDialogFragment.TAG)
        }

        binding.btnLanguage.setOnClickListener {
            languageLauncher.launch(SettingLanguageActivity.newIntent(this))
        }
    }
}
