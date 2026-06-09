package com.wavez.controllerwidget.ui.theme.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.ads.AdsId
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.core.extensions.parcelableArrayList
import com.wavez.controllerwidget.data.model.icon.IconItemModel
import com.wavez.controllerwidget.databinding.ActivityAppliedThemeBinding
import com.wavez.controllerwidget.ui.home.MainActivity
import com.wavez.controllerwidget.ui.home.fragment.theme.adapter.ThemeAdapter
import com.wavez.controllerwidget.ui.premium.PremiumActivity
import com.wavez.controllerwidget.ui.theme.adapter.AppliedThemeIconAdapter
import com.wavez.controllerwidget.ui.theme.viewmodel.AppliedThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppliedThemeActivity : BaseActivity<ActivityAppliedThemeBinding>() {
    override fun bindingView(): ActivityAppliedThemeBinding {
        return ActivityAppliedThemeBinding.inflate(layoutInflater)
    }

    companion object {
        private const val EXTRA_MODE = "extra_mode"
        private const val EXTRA_REMAINING_ICONS = "extra_remaining_icons"
        private const val MODE_SINGLE_ICON = "single_icon"
        private const val MODE_ALL_ICONS = "all_icons"
        private const val MAX_REMAINING_ICONS = 8

        fun singleIconIntent(
            context: Context,
            remainingIcons: List<IconItemModel>,
        ): Intent {
            return Intent(context, AppliedThemeActivity::class.java).apply {
                putExtra(EXTRA_MODE, MODE_SINGLE_ICON)
                putParcelableArrayListExtra(
                    EXTRA_REMAINING_ICONS,
                    ArrayList(remainingIcons.take(MAX_REMAINING_ICONS)),
                )
            }
        }

        fun allIconsIntent(context: Context): Intent {
            return Intent(context, AppliedThemeActivity::class.java).apply {
                putExtra(EXTRA_MODE, MODE_ALL_ICONS)
            }
        }
    }

    private val viewModel by viewModels<AppliedThemeViewModel>()
    private val remainingIconAdapter = AppliedThemeIconAdapter()
    private val suggestedThemeAdapter = ThemeAdapter()

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        binding.rcvRemainingIcons.adapter = remainingIconAdapter
        binding.rcvSuggestedThemes.adapter = suggestedThemeAdapter
        renderMode(intent.getStringExtra(EXTRA_MODE).orEmpty())
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
            NativeAdPlacement.NATIVE_THEME_APPLIED,
            binding.nativeApply,
            shouldShow = sharedPref.shouldShowNativeInApp,
            autoLoad = true,
        )
    }

    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }
        binding.btnHome.setOnClickListener {
            navigateTo(MainActivity.newIntent(this).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        binding.btnViewMoreIcons.setOnClickListener {
            finish()
        }

        binding.btnPremium.setOnClickListener {
            startActivity(PremiumActivity.newIntent(this))
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.suggestedThemes.collectWithLifecycle(this) { state ->
            if (state is UiState.Data) {
                suggestedThemeAdapter.set(state.value)
            }
        }
    }

    private fun renderMode(mode: String) {
        binding.tvAppliedMessage.text = getString(
            R.string.s_applied_successfully,
            getString(R.string.shortcut),
        )
        val isSingleIcon = mode == MODE_SINGLE_ICON
        binding.viewRemainingIcons.isVisible = isSingleIcon
        binding.viewSuggestedThemes.isVisible = !isSingleIcon
        if (isSingleIcon) {
            remainingIconAdapter.set(getRemainingIcons())
        } else {
            viewModel.loadSuggestedThemes()
        }
    }

    private fun getRemainingIcons(): List<IconItemModel> {
        return intent.parcelableArrayList<IconItemModel>(EXTRA_REMAINING_ICONS).orEmpty()
    }
}
