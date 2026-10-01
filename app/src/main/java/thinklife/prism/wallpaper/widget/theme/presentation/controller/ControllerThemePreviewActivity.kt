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
import com.bumptech.glide.Glide
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityControllerThemePreviewBinding
import thinklife.prism.wallpaper.widget.theme.presentation.controller.model.ControllerPreviewEvent
import thinklife.prism.wallpaper.widget.theme.presentation.controller.model.ControllerPreviewUiState
import thinklife.prism.wallpaper.widget.theme.presentation.controller.viewmodel.ControllerThemePreviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ControllerThemePreviewActivity : BaseActivity<ActivityControllerThemePreviewBinding>() {
    override fun bindingView() = ActivityControllerThemePreviewBinding.inflate(layoutInflater)

    companion object {
        private const val EXTRA_THEME_ID = "controller_theme_id"

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
        binding.btnBack.setOnClickListener { finish() }
        binding.btnAction.setOnClickListener { viewModel.onActionClick() }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.uiState.collectWithLifecycle(this, action = ::render)
        viewModel.events.collectWithLifecycle(this) { event ->
            when (event) {
                ControllerPreviewEvent.NotFound -> finish()
                ControllerPreviewEvent.DownloadFailed ->
                    Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show()
                is ControllerPreviewEvent.Applied -> {
                    Toast.makeText(
                        this,
                        getString(R.string.controller_theme_applied, event.themeName),
                        Toast.LENGTH_SHORT,
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun render(state: ControllerPreviewUiState) {
        val theme = state.theme ?: return
        if (loadedThumbnail != theme.thumbnail) {
            loadedThumbnail = theme.thumbnail
            Glide.with(this).load(theme.thumbnail).centerCrop().into(binding.ivPreview)
        }
        binding.btnAction.text = when {
            state.downloading -> getString(R.string.downloading_please_wait_a_moment)
            state.downloaded && state.active -> getString(R.string.controller_theme_active)
            state.downloaded -> getString(R.string.unlock)
            else -> getString(R.string.download)
        }
        binding.btnAction.isEnabled = !state.downloading && !(state.downloaded && state.active)
        binding.progressDownload.isVisible = state.downloading
        binding.progressDownload.progress = state.progress
    }
}
