package com.wavez.themewallpaper.ui.home.fragment.wallpaper.page

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.wavez.themewallpaper.core.base.BaseFragment
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.databinding.FragmentWallpaperCategoryBinding
import com.wavez.themewallpaper.ui.home.fragment.wallpaper.viewmodel.WallpaperPageViewModel
import com.wavez.themewallpaper.ui.home.fragment.wallpaper.adapter.WallpaperAdapter
import com.wavez.themewallpaper.ui.wallpaper.PreviewWallpaperActivity
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
