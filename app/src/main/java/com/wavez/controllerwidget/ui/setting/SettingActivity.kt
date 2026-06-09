package com.wavez.controllerwidget.ui.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.extensions.openUrl
import com.wavez.controllerwidget.databinding.ActivitySettingBinding
import com.wavez.controllerwidget.ui.language.activity.SettingLanguageActivity
import com.wavez.controllerwidget.ui.setting.feedback.FeedbackActivity
import com.wavez.controllerwidget.ui.utils.PRIVACY_POLICY_URL
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

        binding.btnLanguage.setOnClickListener {
            languageLauncher.launch(SettingLanguageActivity.newIntent(this))
        }
    }

    override fun onHandleBackPressed() {
        executeWithAds {
            super.onHandleBackPressed()
        }
    }
}
