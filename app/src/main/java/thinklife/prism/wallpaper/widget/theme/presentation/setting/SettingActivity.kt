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
import dagger.hilt.android.AndroidEntryPoint
import thinklife.prism.wallpaper.widget.theme.BuildConfig
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.extensions.openUrl
import thinklife.prism.wallpaper.widget.theme.data.provider.LanguageProvider
import thinklife.prism.wallpaper.widget.theme.databinding.ActivitySettingBinding
import thinklife.prism.wallpaper.widget.theme.presentation.language.SettingLanguageActivity
import thinklife.prism.wallpaper.widget.theme.presentation.setting.dialog.RateDialogFragment
import thinklife.prism.wallpaper.widget.theme.presentation.setting.feedback.FeedbackActivity
import thinklife.prism.wallpaper.widget.theme.util.PRIVACY_POLICY_URL

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    override fun inflateBinding() = ActivitySettingBinding.inflate(layoutInflater)

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SettingActivity::class.java)
        }
    }

    override val adsScreenId = AdsScreen.SETTINGS_VIEW

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
        updateCurrentLanguage()
        binding.tvAppVersion.text = getString(R.string.app_version_format, BuildConfig.VERSION_NAME)
    }

    override fun onResume() {
        super.onResume()
        updateCurrentLanguage()
    }

    override fun initListener() {
        super.initListener()

        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }

        binding.btnPrivacy.setOnClickListener {
            triggerAds(AdsTrigger.SETTINGS_CLICK_PRIVACY) {
                runWhenResumed {
                    openUrl(PRIVACY_POLICY_URL)
                }
            }
        }

        binding.btnFeedback.setOnClickListener {
            triggerAds(AdsTrigger.SETTINGS_CLICK_FEEDBACK) {
                runWhenResumed {
                    startActivity(FeedbackActivity.newIntent(this))
                }
            }
        }

        binding.btnRate.setOnClickListener {
            triggerAds(AdsTrigger.SETTINGS_CLICK_RATE) {
                runWhenResumed {
                    RateDialogFragment.newInstance(
                        preSelectedStars = if (sharedPref.isRate) sharedPref.ratedStars else 0
                    ).show(supportFragmentManager, RateDialogFragment.TAG)
                }
            }
        }

        binding.btnLanguage.setOnClickListener {
            triggerAds(AdsTrigger.SETTINGS_CLICK_LANGUAGE) {
                runWhenResumed {
                    languageLauncher.launch(SettingLanguageActivity.newIntent(this))
                }
            }
        }
    }

    private fun updateCurrentLanguage() {
        val currentTag = sharedPref.getConfigLanguage()
        val languageItem = LanguageProvider.findByTag(currentTag)
        if (languageItem != null) {
            binding.tvCurrentLanguage.setText(languageItem.nameRes)
        } else {
            binding.tvCurrentLanguage.text = currentTag
        }
    }
    override fun onHandleBackPressed() {
        triggerBackAds(AdsTrigger.SETTINGS_CLICK_BACK) { finish() }
    }

}
