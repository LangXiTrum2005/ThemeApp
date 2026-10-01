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

package thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.page

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentWallpaperCategoryBinding
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.viewmodel.WallpaperPageViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.adapter.WallpaperAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.PreviewWallpaperActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WallpaperCategoryFragment : BaseFragment<FragmentWallpaperCategoryBinding>() {
    override fun bindingView(): FragmentWallpaperCategoryBinding {
        return FragmentWallpaperCategoryBinding.inflate(layoutInflater)
    }

    companion object {
        const val ARG_CATEGORY_ID = "category_id"

        fun newInstance(categoryId: String) =
            WallpaperCategoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_ID, categoryId)
                }
            }
    }

    private val categoryId: String by lazy {
        arguments?.getString(ARG_CATEGORY_ID) ?: ""
    }

    private val wallpaperAdapter = WallpaperAdapter()

    private val viewModel by viewModels<WallpaperPageViewModel>()

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.rcvWallpaper.adapter = wallpaperAdapter
        viewModel.getWallpapersByCategory(categoryId)
    }

    override fun initListener() {
        super.initListener()

        wallpaperAdapter.onClickItem = { wallpaper ->
            startActivity(PreviewWallpaperActivity.newIntent(requireContext(), wallpaper))
        }

        binding.viewError.setOnRetryClickListener {
            viewModel.getWallpapersByCategory(categoryId, forceRefresh = true)
        }
    }

    override fun initObserver() {
        super.initObserver()

        viewModel.wallpapersState.collectWithLifecycle(this) {
            when (it) {
                is UiState.Error -> {
                    Log.d("log_error", "initObserver: ${it.throwable}")
                    setViewState(showContent = false, showLoading = false, showError = true)
                }

                UiState.Loading -> {
                    setViewState(showContent = false, showLoading = true, showError = false)
                }

                is UiState.Data -> {
                    setViewState(showContent = true, showLoading = false, showError = false)
                    wallpaperAdapter.set(it.value)
                }
                else -> {}
            }
        }
    }

    private fun setViewState(showContent: Boolean, showLoading: Boolean, showError: Boolean) {
        binding.rcvWallpaper.isVisible = showContent
        binding.viewLoading.isVisible = showLoading
        binding.viewError.isVisible = showError
    }
}
