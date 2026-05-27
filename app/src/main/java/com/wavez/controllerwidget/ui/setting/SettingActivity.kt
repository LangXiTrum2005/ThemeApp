package com.wavez.controllerwidget.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.net.toUri
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.extensions.openUrl
import com.wavez.controllerwidget.databinding.ActivitySettingBinding
import com.wavez.controllerwidget.ui.language.activity.SettingLanguageActivity
import com.wavez.controllerwidget.ui.setting.feedback.FeedbackActivity
import com.wavez.controllerwidget.ui.utils.EMAIL
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
            startActivity(SettingLanguageActivity.newIntent(this))
        }
    }
}
