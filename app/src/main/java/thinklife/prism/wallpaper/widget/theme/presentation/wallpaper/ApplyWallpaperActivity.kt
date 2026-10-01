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

package thinklife.prism.wallpaper.widget.theme.presentation.wallpaper

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.getParcel
import thinklife.prism.wallpaper.widget.theme.data.model.wallpaper.WallpaperModel
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityApplyWallpaperBinding
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.adapter.WallpaperAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.setting.dialog.RatePromptManager
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.viewmodel.ApplyWallpaperViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ApplyWallpaperActivity : BaseActivity<ActivityApplyWallpaperBinding>() {
    override fun bindingView(): ActivityApplyWallpaperBinding {
        return ActivityApplyWallpaperBinding.inflate(layoutInflater)
    }

    companion object {
        const val ARG_WALLPAPER = "arg_wallpaper"

        fun newIntent(context: Context, wallpaperModel: WallpaperModel): Intent {
            return Intent(context, ApplyWallpaperActivity::class.java).apply {
                putExtra(ARG_WALLPAPER, wallpaperModel)
            }
        }
    }

    private val wallpaperModel: WallpaperModel? by lazy {
        intent.getParcel(ARG_WALLPAPER)
    }

    private val wallpaperAdapter = WallpaperAdapter()

    private val viewModel by viewModels<ApplyWallpaperViewModel>()

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        viewModel.currentWallpaper = wallpaperModel
        viewModel.getWallpapersByCategory()
        binding.rcvWallpaper.adapter = wallpaperAdapter
        if (savedInstanceState == null) {
            RatePromptManager.showAfterSuccessfulSetup(supportFragmentManager, sharedPref)
        }
    }



    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        wallpaperModel?.let { wallpaper ->
            Glide.with(this).load(wallpaper.url)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        p0: GlideException?, p1: Any?, p2: Target<Drawable?>?, p3: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        p0: Drawable?,
                        p1: Any?,
                        p2: Target<Drawable?>?,
                        p3: DataSource?,
                        p4: Boolean
                    ): Boolean {
                        return false
                    }
                }).into(binding.ivImage)
        }
    }

    override fun initListener() {
        super.initListener()

        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }

        wallpaperAdapter.onClickItem = { wallpaper ->
            startActivity(PreviewWallpaperActivity.newIntent(this, wallpaper))
        }

        binding.btnHome.setOnClickListener {
            navigateTo(MainActivity.newIntent(this).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
    }

    override fun initObserver() {
        super.initObserver()
        lifecycleScope.launch {
            viewModel.wallpapersState.collect {
                when (it) {
                    is UiState.Error -> {}
                    UiState.Loading -> {}
                    is UiState.Data -> {
                        wallpaperAdapter.set(it.value)
                    }

                    else -> {}
                }
            }
        }
    }
}
