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
            sendFeedbackEmail(this)
        }
    }


    private fun sendFeedbackEmail(
        context: Context,
        email: String = EMAIL,
        subject: String = getString(R.string.app_name)
    ) {
        val body = buildString {
            appendLine()
            appendLine()
            appendLine("----- Device Info -----")
            appendLine("App package: ${context.packageName}")
            appendLine("Android: ${Build.VERSION.RELEASE}")
            appendLine("SDK: ${Build.VERSION.SDK_INT}")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:$email".toUri()
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        context.startActivity(intent)
    }

}
