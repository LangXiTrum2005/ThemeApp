/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.presentation.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.extensions.openUrl
import thinklife.prism.wallpaper.widget.theme.databinding.ActivitySettingBinding
import thinklife.prism.wallpaper.widget.theme.presentation.language.LanguageActivity
import thinklife.prism.wallpaper.widget.theme.presentation.setting.dialog.RateDialogFragment
import thinklife.prism.wallpaper.widget.theme.presentation.setting.feedback.FeedbackActivity
import thinklife.prism.wallpaper.widget.theme.util.PRIVACY_POLICY_URL
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
            languageLauncher.launch(LanguageActivity.newIntent(this))
        }
    }
}
