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

package thinklife.prism.wallpaper.widget.theme.presentation.controllertutorial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.PermissionHelper
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityControllerTutorialBinding

@AndroidEntryPoint
class ControllerTutorialActivity : BaseActivity<ActivityControllerTutorialBinding>() {

    companion object {
        fun newIntent(context: Context) = Intent(context, ControllerTutorialActivity::class.java)
    }

    private val viewModel: ControllerTutorialViewModel by viewModels()

    override val adsScreenId = AdsScreen.CONTROLLER_TUTORIAL_VIEW

    override fun inflateBinding() = ActivityControllerTutorialBinding.inflate(layoutInflater)

    override fun initConfig(savedInstanceState: Bundle?) {
        binding.tvStepSelectApp.text = getString(
            R.string.controller_tutorial_step_select_app,
            getString(R.string.app_name),
        )
    }

    override fun initListener() {
        binding.btnBack.setOnClickListener { onHandleBackPressed() }
        binding.btnEnableController.setOnClickListener {
            triggerAds(AdsTrigger.CONTROLLER_TUTORIAL_CLICK_ENABLE) {
                runWhenResumed { viewModel.onEnableControllerClicked() }
            }
        }
    }

    override fun initObserver() {
        viewModel.controllerEnabled.collectWithLifecycle(this) { enabled ->
            if (enabled) runWhenResumed { finish() }
        }
        viewModel.openAccessibilitySettings.collectWithLifecycle(this) {
            runWhenResumed { PermissionHelper.openAccessibilitySettings(this) }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissionState()
    }

    override fun onHandleBackPressed() {
        triggerAds(AdsTrigger.CONTROLLER_TUTORIAL_CLICK_BACK) {
            runWhenResumed { finish() }
        }
    }
}

