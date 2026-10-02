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

package thinklife.prism.wallpaper.widget.theme.presentation.controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.core.extensions.loadWithLoading
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityControllerThemePreviewBinding
import thinklife.prism.wallpaper.widget.theme.presentation.controller.model.ControllerPreviewEvent
import thinklife.prism.wallpaper.widget.theme.presentation.controller.model.ControllerPreviewUiState
import thinklife.prism.wallpaper.widget.theme.presentation.controller.viewmodel.ControllerThemePreviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ControllerThemePreviewActivity : BaseActivity<ActivityControllerThemePreviewBinding>() {
    override fun inflateBinding() = ActivityControllerThemePreviewBinding.inflate(layoutInflater)

    override val adsScreenId = AdsScreen.CONTROLLER_PREVIEW_VIEW

    companion object {
        private const val EXTRA_THEME_ID = "controller_theme_id"
        private const val ENABLED_ACTION_ALPHA = 1f
        private const val DISABLED_ACTION_ALPHA = 0.55f

        fun newIntent(context: Context, themeId: String) =
            Intent(context, ControllerThemePreviewActivity::class.java).putExtra(EXTRA_THEME_ID, themeId)
    }

    private val viewModel: ControllerThemePreviewViewModel by viewModels()

    // Tránh gọi Glide lại mỗi lần state đổi (progress tải cập nhật liên tục).
    private var loadedThumbnail: String? = null

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        viewModel.load(intent.getStringExtra(EXTRA_THEME_ID).orEmpty())
    }

    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener { onHandleBackPressed() }
        binding.btnAction.setOnClickListener {
            triggerRewardAds(AdsTrigger.CONTROLLER_PREVIEW_CLICK_ACTION) {
                runWhenResumed { viewModel.onActionClick() }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.uiState.collectWithLifecycle(this, action = ::render)
        viewModel.events.collectWithLifecycle(this) { event ->
            when (event) {
                ControllerPreviewEvent.NotFound -> runWhenResumed { finish() }
                ControllerPreviewEvent.DownloadFailed ->
                    Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show()
                is ControllerPreviewEvent.Applied -> {
                    Toast.makeText(
                        this,
                        getString(R.string.controller_theme_applied, event.themeName),
                        Toast.LENGTH_SHORT,
                    ).show()
                    runWhenResumed { finish() }
                }
            }
        }
    }

    private fun render(state: ControllerPreviewUiState) {
        state.theme?.let { theme ->
            binding.tvThemeName.text = theme.name
            binding.ivPreview.contentDescription =
                getString(R.string.controller_preview_image_description, theme.name)
            if (loadedThumbnail != theme.thumbnail) {
                loadedThumbnail = theme.thumbnail
                loadPreviewImage(theme.thumbnail)
            }
        }
        binding.tvThemeStatus.setText(state.statusLabelRes)
        binding.btnAction.setText(state.actionLabelRes)
        binding.btnAction.isEnabled = state.actionEnabled
        binding.btnAction.isAdIconVisible = state.actionEnabled
        binding.btnAction.alpha =
            if (state.actionEnabled) ENABLED_ACTION_ALPHA else DISABLED_ACTION_ALPHA
        binding.progressDownload.isVisible = state.downloading
        binding.progressDownload.progress = state.progress
        binding.tvDownloadProgress.isVisible = state.downloading
        binding.tvDownloadProgress.text =
            getString(R.string.controller_preview_download_progress, state.progress)
    }

    private fun loadPreviewImage(thumbnail: String) {
        binding.ivPreview.loadWithLoading(thumbnail, binding.previewLoading.root)
        binding.previewLoading.errorGroup.setOnClickListener {
            triggerAds(AdsTrigger.CONTROLLER_PREVIEW_CLICK_RETRY) {
                runWhenResumed { loadPreviewImage(thumbnail) }
            }
        }
    }

    override fun onHandleBackPressed() {
        triggerBackAds(AdsTrigger.CONTROLLER_PREVIEW_CLICK_BACK) {
            runWhenResumed { finish() }
        }
    }
}
