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

package thinklife.prism.wallpaper.widget.theme.presentation.theme.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.core.extensions.parcelableArrayList
import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconItemModel
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityAppliedThemeBinding
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.theme.adapter.ThemeAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.setting.dialog.RatePromptManager
import thinklife.prism.wallpaper.widget.theme.presentation.theme.adapter.AppliedThemeIconAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.theme.viewmodel.AppliedThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppliedThemeActivity : BaseActivity<ActivityAppliedThemeBinding>() {
    override fun bindingView(): ActivityAppliedThemeBinding {
        return ActivityAppliedThemeBinding.inflate(layoutInflater)
    }

    companion object {
        private const val EXTRA_MODE = "extra_mode"
        private const val EXTRA_REMAINING_ICONS = "extra_remaining_icons"
        private const val MODE_SINGLE_ICON = "single_icon"
        private const val MODE_ALL_ICONS = "all_icons"
        private const val MAX_REMAINING_ICONS = 8

        fun singleIconIntent(
            context: Context,
            remainingIcons: List<IconItemModel>,
        ): Intent {
            return Intent(context, AppliedThemeActivity::class.java).apply {
                putExtra(EXTRA_MODE, MODE_SINGLE_ICON)
                putParcelableArrayListExtra(
                    EXTRA_REMAINING_ICONS,
                    ArrayList(remainingIcons.take(MAX_REMAINING_ICONS)),
                )
            }
        }

        fun allIconsIntent(context: Context): Intent {
            return Intent(context, AppliedThemeActivity::class.java).apply {
                putExtra(EXTRA_MODE, MODE_ALL_ICONS)
            }
        }
    }

    private val viewModel by viewModels<AppliedThemeViewModel>()
    private val remainingIconAdapter = AppliedThemeIconAdapter()
    private val suggestedThemeAdapter = ThemeAdapter()

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        binding.rcvRemainingIcons.adapter = remainingIconAdapter
        binding.rcvSuggestedThemes.adapter = suggestedThemeAdapter
        renderMode(intent.getStringExtra(EXTRA_MODE).orEmpty())
        if (savedInstanceState == null) {
            RatePromptManager.showAfterSuccessfulSetup(supportFragmentManager, sharedPref)
        }
    }



    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }
        binding.btnHome.setOnClickListener {
            navigateTo(MainActivity.newIntent(this).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        binding.btnViewMoreIcons.setOnClickListener {
            finish()
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.suggestedThemes.collectWithLifecycle(this) { state ->
            if (state is UiState.Data) {
                suggestedThemeAdapter.set(state.value)
            }
        }
    }

    private fun renderMode(mode: String) {
        binding.tvAppliedMessage.text = getString(
            R.string.s_applied_successfully,
            getString(R.string.shortcut),
        )
        val isSingleIcon = mode == MODE_SINGLE_ICON
        binding.viewRemainingIcons.isVisible = isSingleIcon
        binding.viewSuggestedThemes.isVisible = !isSingleIcon
        if (isSingleIcon) {
            remainingIconAdapter.set(getRemainingIcons())
        } else {
            viewModel.loadSuggestedThemes()
        }
    }

    private fun getRemainingIcons(): List<IconItemModel> {
        return intent.parcelableArrayList<IconItemModel>(EXTRA_REMAINING_ICONS).orEmpty()
    }
}
