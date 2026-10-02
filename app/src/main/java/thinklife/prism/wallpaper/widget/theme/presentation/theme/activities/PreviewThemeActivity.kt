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

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.core.extensions.loadWithLoading
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeModel
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityPreviewThemeBinding
import thinklife.prism.wallpaper.widget.theme.presentation.theme.viewmodel.PreviewThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreviewThemeActivity : BaseActivity<ActivityPreviewThemeBinding>() {

    override val adsScreenId = AdsScreen.PREVIEW_THEME_VIEW

    override fun bindingView(): ActivityPreviewThemeBinding {
        return ActivityPreviewThemeBinding.inflate(layoutInflater)
    }

    companion object {
        private const val ARG_THEME_ID = "arg_theme_id"

        fun newIntent(context: Context, themeId: String): Intent {
            return Intent(context, PreviewThemeActivity::class.java).apply {
                putExtra(ARG_THEME_ID, themeId)
            }
        }
    }

    private val viewModel by viewModels<PreviewThemeViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        viewModel.loadTheme(intent.getStringExtra(ARG_THEME_ID).orEmpty())
    }



    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }
        binding.btnGetThemes.setOnClickListener {
            triggerAds(AdsTrigger.PREVIEW_THEME_CLICK_GET_THEMES) {
                runWhenResumed {
                    viewModel.continueToGetTheme()
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.themeState.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Data -> renderTheme(state.value)
                is UiState.Loading -> {
                    binding.viewWallpaperLoading.root.isVisible = true
                    binding.viewThemeLoading.root.isVisible = true
                    binding.btnGetThemes.isEnabled = false
                }

                is UiState.Error -> {
                    binding.viewWallpaperLoading.root.isVisible = false
                    binding.viewThemeLoading.root.isVisible = false
                    binding.btnGetThemes.isEnabled = false
                    Toast.makeText(this, R.string.server_error_message, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }

        viewModel.saveEvent.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Data -> {
                    viewModel.currentTheme?.let { theme ->
                        startActivity(GetThemeActivity.newIntent(this, theme))
                    }
                }

                is UiState.Error -> {
                    Toast.makeText(this, R.string.theme_save_failed, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }

    private fun renderTheme(theme: ThemeModel) {
        binding.btnGetThemes.isEnabled = true
        binding.ivWallpaper.loadWithLoading(
            theme.wallpaper.thumb.ifBlank { theme.wallpaper.url },
            binding.viewWallpaperLoading.root,
        )
        binding.ivTheme.loadWithLoading(
            theme.thumb,
            binding.viewThemeLoading.root,
        )
    }
    override fun onHandleBackPressed() {
        triggerBackAds(AdsTrigger.PREVIEW_THEME_CLICK_BACK) { finish() }
    }

}
