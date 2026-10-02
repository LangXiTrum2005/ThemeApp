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
import androidx.activity.viewModels
import androidx.core.view.isVisible
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.core.extensions.loadWithLoading
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityDetailBannerBinding
import thinklife.prism.wallpaper.widget.theme.presentation.theme.adapter.ThemeAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.theme.viewmodel.ThemePageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailBannerActivity : BaseActivity<ActivityDetailBannerBinding>() {

    override val adsScreenId = AdsScreen.DETAIL_BANNER_VIEW


    override fun bindingView(): ActivityDetailBannerBinding {
        return ActivityDetailBannerBinding.inflate(layoutInflater)
    }

    companion object {
        private const val EXTRA_CATEGORY_ID = "extra_category_id"
        private const val EXTRA_BANNER_URL = "extra_banner_url"

        fun newIntent(
            context: Context,
            categoryId: String,
            bannerUrl: String,
        ): Intent {
            return Intent(context, DetailBannerActivity::class.java).apply {
                putExtra(EXTRA_CATEGORY_ID, categoryId)
                putExtra(EXTRA_BANNER_URL, bannerUrl)
            }
        }
    }

    private val viewModel by viewModels<ThemePageViewModel>()
    private val themeAdapter = ThemeAdapter()
    private val categoryId: String by lazy { intent.getStringExtra(EXTRA_CATEGORY_ID).orEmpty() }
    private val bannerUrl: String by lazy { intent.getStringExtra(EXTRA_BANNER_URL).orEmpty() }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.ivBanner.loadWithLoading(bannerUrl, binding.viewBannerLoading.root)
        binding.rcvTheme.adapter = themeAdapter
    }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        viewModel.getThemesByCategory(categoryId)
    }



    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }
        themeAdapter.onClickItem = { theme ->
            triggerAds(AdsTrigger.DETAIL_BANNER_CLICK_THEME_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    startActivity(PreviewThemeActivity.newIntent(this, theme.id))
                }
            }
        }
        binding.viewError.setOnRetryClickListener {
            triggerAds(AdsTrigger.DETAIL_BANNER_CLICK_ERROR_RETRY) {
                runWhenResumed {
                    viewModel.getThemesByCategory(categoryId, forceRefresh = true)
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.themesState.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Data -> {
                    binding.viewLoading.isVisible = false
                    binding.viewError.isVisible = false
                    binding.rcvTheme.isVisible = true
                    themeAdapter.set(state.value)
                }

                is UiState.Error -> {
                    binding.viewLoading.isVisible = false
                    binding.viewError.isVisible = true
                    binding.rcvTheme.isVisible = false
                }

                UiState.Loading -> {
                    binding.viewLoading.isVisible = true
                    binding.viewError.isVisible = false
                    binding.rcvTheme.isVisible = false
                }

                else -> Unit
            }
        }
    }

    override fun onHandleBackPressed() {
        triggerBackAds(AdsTrigger.DETAIL_BANNER_CLICK_BACK) { finish() }
    }

}
