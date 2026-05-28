package com.wavez.controllerwidget.ui.theme.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.ads.AdsId
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.core.extensions.loadWithLoading
import com.wavez.controllerwidget.data.model.theme.ThemeModel
import com.wavez.controllerwidget.databinding.ActivityPreviewThemeBinding
import com.wavez.controllerwidget.ui.theme.viewmodel.PreviewThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreviewThemeActivity : BaseActivity<ActivityPreviewThemeBinding>() {
    override fun bindingView(): ActivityPreviewThemeBinding {
        return ActivityPreviewThemeBinding.inflate(layoutInflater)
    }

    companion object {
        private const val ARG_THEME_ID = "arg_theme_id"

        private const val ARG_IS_HOME = "arg_is_home"

        fun newIntent(context: Context, themeId: String, isHome: Boolean = false): Intent {
            return Intent(context, PreviewThemeActivity::class.java).apply {
                putExtra(ARG_THEME_ID, themeId)
                putExtra(ARG_IS_HOME, isHome)
            }
        }
    }

    private val viewModel by viewModels<PreviewThemeViewModel>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        viewModel.loadTheme(intent.getStringExtra(ARG_THEME_ID).orEmpty())
        loadBanner()
    }

    private fun loadBanner() {
        binding.bannerAds.registerBanner(
            this, adId = AdsId.getBannerInAppAds(),
            shouldLoad = sharedPref.shouldShowBannerInApp && !sharedPref.isVip,
        )
    }

    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }
        binding.btnGetThemes.setOnClickListener {
            viewModel.continueToGetTheme()
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
        val isHome = intent.getBooleanExtra(ARG_IS_HOME, false)
        if (!isHome) {
            super.onHandleBackPressed()
        } else {
            executeWithAds {
                super.onHandleBackPressed()
            }
        }
    }
}
