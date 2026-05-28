package com.wavez.controllerwidget.ui.iconpack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.wavez.controllerwidget.ads.AdsId
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.databinding.ActivityApplyIconsBinding
import com.wavez.controllerwidget.ui.home.fragment.icons.adapter.IconAdapter
import com.wavez.controllerwidget.ui.iconpack.viewmodel.ApplyIconsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ApplyIconsActivity : BaseActivity<ActivityApplyIconsBinding>() {
    override fun bindingView(): ActivityApplyIconsBinding {
        return ActivityApplyIconsBinding.inflate(layoutInflater)
    }

    companion object {
        const val ARG_ICON_PACK_ID = "arg_icon"

        fun newIntent(context: Context, iconPackId: String): Intent {
            return Intent(context, ApplyIconsActivity::class.java).apply {
                putExtra(ARG_ICON_PACK_ID, iconPackId)
            }
        }
    }

    private val iconId: String? by lazy {
        intent.getStringExtra(ARG_ICON_PACK_ID)
    }

    private val iconAdapter = IconAdapter(isApply = true)

    private val viewModel by viewModels<ApplyIconsViewModel>()

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        viewModel.currentIconsId = iconId
        viewModel.getIcons()
        binding.rcvIcons.adapter = iconAdapter
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
            NativeAdPlacement.NATIVE_ICON_APPLIED,
            binding.nativeIcon,
            shouldShow = sharedPref.shouldShowNativeInApp,
            autoLoad = true,
        )
    }


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    override fun initListener() {
        super.initListener()

        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }

        iconAdapter.onClickItem = { pack ->
            startActivity(
                SetIconsActivity.newIntent(
                    this, pack.id, pack.name
                )
            )
        }
    }

    override fun initObserver() {
        super.initObserver()
        lifecycleScope.launch {
            viewModel.iconsState.collect {
                when (it) {
                    is UiState.Error -> {
                        Log.d("log_icon", "initObserver: ${it.message}")

                    }
                    UiState.Loading -> {}
                    is UiState.Data -> {
                        Log.d("log_icon", "initObserver: ${it.value}")
                        iconAdapter.set(it.value)
                    }

                    else -> {}
                }
            }
        }
    }
}