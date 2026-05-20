package com.wavez.controllerwidget.ui.home.fragment.wallpaper.page

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.m65_gamemodefps.core.base.BaseFragment
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.databinding.FragmentWallpaperCategoryBinding
import com.wavez.controllerwidget.ui.home.fragment.wallpaper.viewmodel.WallpaperPageViewModel
import com.wavez.controllerwidget.ui.home.fragment.wallpaper.adapter.WallpaperAdapter
import com.wavez.controllerwidget.ui.wallpaper.PreviewWallpaperActivity
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
                    binding.viewError.isVisible = true
                    binding.rcvWallpaper.isVisible = false
                }

                UiState.Loading -> {
                    binding.viewError.isVisible = false
                }

                is UiState.Data -> {
                    binding.viewError.isVisible = false
                    binding.rcvWallpaper.isVisible = true
                    wallpaperAdapter.set(it.value)
                }
                else -> {}
            }
        }
    }
}
