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

package com.onetabb.template.android.presentation.welcomeback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.onetabb.template.android.ads.AdsScreen
import com.onetabb.template.android.ads.AdsTrigger
import com.onetabb.template.android.core.base.BaseActivity
import com.onetabb.template.android.databinding.ActivityWelcomeBackBinding
import com.onetabb.template.android.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeBackActivity : BaseActivity<ActivityWelcomeBackBinding>() {

    private var fromResume: Boolean = false

    override fun inflateBinding() = ActivityWelcomeBackBinding.inflate(layoutInflater)

    override val adsScreenId = AdsScreen.WELCOME_BACK_VIEW

    override fun initConfig(savedInstanceState: Bundle?) {
        fromResume = intent.getBooleanExtra(EXTRA_FROM_RESUME, false)
    }

    override fun initListener() {
        binding.btnContinue.setOnClickListener {
            triggerAds(AdsTrigger.WELCOME_BACK_CLICK_CONTINUE) {
                runWhenResumed {
                    if (fromResume) {
                        finish()
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_FROM_RESUME = "extra_from_resume"

        fun newIntent(
            context: Context,
            fromResume: Boolean = false,
        ): Intent = Intent(context, WelcomeBackActivity::class.java).apply {
            putExtra(EXTRA_FROM_RESUME, fromResume)
        }
    }

    override fun onHandleBackPressed() {
    }
}
