package com.wavez.themewallpaper.ui.wallpaper

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
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.extensions.getParcel
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel
import com.wavez.themewallpaper.databinding.ActivityApplyWallpaperBinding
import com.wavez.themewallpaper.ui.home.MainActivity
import com.wavez.themewallpaper.ui.home.fragment.wallpaper.adapter.WallpaperAdapter
import com.wavez.themewallpaper.ui.setting.dialog.RatePromptManager
import com.wavez.themewallpaper.ui.wallpaper.viewmodel.ApplyWallpaperViewModel
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
