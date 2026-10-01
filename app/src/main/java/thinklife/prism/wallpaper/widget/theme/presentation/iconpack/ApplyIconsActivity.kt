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

package thinklife.prism.wallpaper.widget.theme.presentation.iconpack

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
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityApplyIconsBinding
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.icons.adapter.IconAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.viewmodel.ApplyIconsViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.setting.dialog.RatePromptManager
import thinklife.prism.wallpaper.widget.theme.presentation.theme.adapter.AppliedThemeIconAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ApplyIconsActivity : BaseActivity<ActivityApplyIconsBinding>() {

    override fun bindingView(): ActivityApplyIconsBinding {
        return ActivityApplyIconsBinding.inflate(layoutInflater)
    }

    companion object {
        private const val EXTRA_MODE = "extra_mode"
        private const val EXTRA_REMAINING_ICONS = "extra_remaining_icons"
        private const val MODE_SINGLE_ICON = "single_icon"
        private const val MODE_ALL_ICONS = "all_icons"
        private const val MAX_REMAINING_ICONS = 8
        private const val EXTRA_ICON_PACK_ID = "extra_icon_pack_id"

        fun singleIconIntent(
            context: Context,
            remainingIcons: List<IconItemModel>,
        ): Intent {
            return Intent(context, ApplyIconsActivity::class.java).apply {
                putExtra(EXTRA_MODE, MODE_SINGLE_ICON)
                putParcelableArrayListExtra(
                    EXTRA_REMAINING_ICONS,
                    ArrayList(remainingIcons.take(MAX_REMAINING_ICONS)),
                )
            }
        }

        fun allIconsIntent(context: Context, iconPackID : String): Intent {
            return Intent(context, ApplyIconsActivity::class.java).apply {
                putExtra(EXTRA_ICON_PACK_ID, iconPackID)
                putExtra(EXTRA_MODE, MODE_ALL_ICONS)
            }
        }
    }

    private val viewModel by viewModels<ApplyIconsViewModel>()
    private val remainingIconAdapter = AppliedThemeIconAdapter()
    private val suggestedIcon = IconAdapter(true)

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        binding.rcvRemainingIcons.adapter = remainingIconAdapter
        binding.rcvSuggestedThemes.adapter = suggestedIcon
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
        }    }

    override fun initObserver() {
        super.initObserver()
        viewModel.iconsState.collectWithLifecycle(this) { state ->
            if (state is UiState.Data) {
                suggestedIcon.set(state.value)
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
        viewModel.currentIconsId = intent.getStringExtra(EXTRA_ICON_PACK_ID).orEmpty()
        if (isSingleIcon) {
            remainingIconAdapter.set(
                intent.parcelableArrayList<IconItemModel>(EXTRA_REMAINING_ICONS).orEmpty()
            )
        } else {
            viewModel.getIcons()
        }
    }


}
