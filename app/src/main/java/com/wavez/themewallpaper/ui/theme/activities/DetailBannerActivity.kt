package com.wavez.themewallpaper.ui.theme.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.wavez.themewallpaper.ads.AdsId
import com.wavez.themewallpaper.ads.native_ad.NativeAdPlacement
import com.wavez.themewallpaper.core.base.BaseActivity
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.extensions.collectWithLifecycle
import com.wavez.themewallpaper.core.extensions.loadWithLoading
import com.wavez.themewallpaper.databinding.ActivityDetailBannerBinding
import com.wavez.themewallpaper.ui.home.fragment.theme.adapter.ThemeAdapter
import com.wavez.themewallpaper.ui.home.fragment.theme.viewmodel.ThemePageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailBannerActivity : BaseActivity<ActivityDetailBannerBinding>() {

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
        initNative()
        loadBanner()
    }

    private fun loadBanner() {
        binding.bannerAds.registerBanner(
            this, adId = AdsId.getBannerInAppAds(),
            shouldLoad = sharedPref.shouldShowBannerInApp ,
        )
    }

    private fun initNative() {
        setupNative(
            NativeAdPlacement.NATIVE_BANNER_DETAIL,
            binding.nativeBanner,
            shouldShow = sharedPref.shouldShowNativeInApp,
            autoLoad = true,
        )
    }

    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }
        themeAdapter.onClickItem = { theme ->
            startActivity(PreviewThemeActivity.newIntent(this, theme.id))
        }
        binding.viewError.setOnRetryClickListener {
            viewModel.getThemesByCategory(categoryId, forceRefresh = true)
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
        executeWithAds {
            super.onHandleBackPressed()
        }
    }

}
