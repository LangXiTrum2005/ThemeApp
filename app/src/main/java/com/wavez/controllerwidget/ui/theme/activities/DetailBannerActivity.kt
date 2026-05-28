package com.wavez.controllerwidget.ui.theme.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.wavez.controllerwidget.ads.AdsId
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.core.extensions.loadWithLoading
import com.wavez.controllerwidget.databinding.ActivityDetailBannerBinding
import com.wavez.controllerwidget.ui.home.fragment.theme.adapter.ThemeAdapter
import com.wavez.controllerwidget.ui.home.fragment.theme.viewmodel.ThemePageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailBannerActivity : BaseActivity<ActivityDetailBannerBinding>() {

    override fun bindingView(): ActivityDetailBannerBinding {
        return ActivityDetailBannerBinding.inflate(layoutInflater)
    }

    companion object {
        private const val EXTRA_CATEGORY_ID = "extra_category_id"
        private const val EXTRA_CATEGORY_NAME = "extra_category_name"
        private const val EXTRA_BANNER_URL = "extra_banner_url"

        fun newIntent(
            context: Context,
            categoryId: String,
            categoryName: String,
            bannerUrl: String,
        ): Intent {
            return Intent(context, DetailBannerActivity::class.java).apply {
                putExtra(EXTRA_CATEGORY_ID, categoryId)
                putExtra(EXTRA_CATEGORY_NAME, categoryName)
                putExtra(EXTRA_BANNER_URL, bannerUrl)
            }
        }
    }

    private val viewModel by viewModels<ThemePageViewModel>()
    private val themeAdapter = ThemeAdapter()
    private val categoryId: String by lazy { intent.getStringExtra(EXTRA_CATEGORY_ID).orEmpty() }
    private val categoryName: String by lazy {
        intent.getStringExtra(EXTRA_CATEGORY_NAME).orEmpty()
    }
    private val bannerUrl: String by lazy { intent.getStringExtra(EXTRA_BANNER_URL).orEmpty() }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.tvTitle.text = categoryName
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
            shouldLoad = sharedPref.shouldShowBannerInApp && !sharedPref.isVip,
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
