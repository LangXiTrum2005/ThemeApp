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

package thinklife.prism.wallpaper.widget.theme.presentation.controller

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentControllerBinding
import thinklife.prism.wallpaper.widget.theme.presentation.controller.viewmodel.ControllerViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.controllertutorial.ControllerTutorialActivity
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.setting.SettingActivity

@AndroidEntryPoint
class ControllerFragment : BaseFragment<FragmentControllerBinding>() {

    override fun bindingView(): FragmentControllerBinding =
        FragmentControllerBinding.inflate(layoutInflater)

    companion object {
        private const val THEME_GRID_SPAN_COUNT = 2

        fun newInstance(): ControllerFragment = ControllerFragment()
    }

    override val adsScreenId = AdsScreen.CONTROLLER_VIEW

    private val viewModel: ControllerViewModel by viewModels()

    private val themeAdapter = ControllerThemeAdapter { theme ->
        triggerAds(AdsTrigger.CONTROLLER_CLICK_THEME) {
            runWhenResumed {
                startActivity(ControllerThemePreviewActivity.newIntent(requireContext(), theme.id))
            }
        }
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.tvTitle.setText(R.string.controller)
        binding.rvControllerThemes.apply {
            layoutManager = GridLayoutManager(requireContext(), THEME_GRID_SPAN_COUNT)
            adapter = themeAdapter
        }
        viewModel.loadThemesIfNeeded()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.themesState.collectWithLifecycle(this) { state ->
            if (state is UiState.Data) {
                themeAdapter.submit(state.value, viewModel.activeTheme.value?.id)
                binding.rvControllerThemes.scheduleLayoutAnimation()
            }
        }
        viewModel.activeTheme.collectWithLifecycle(this) { theme ->
            theme?.let(themeAdapter::markApplied)
        }
        viewModel.showEnableControllerCard.collectWithLifecycle(this) { showCard ->
            binding.cardEnableController.isVisible = showCard
        }
    }

    override fun initListener() {
        super.initListener()
        binding.toolbar.btnSetting.setOnClickListener {
            triggerAds(AdsTrigger.CONTROLLER_CLICK_SETTINGS) {
                runWhenResumed { startActivity(SettingActivity.newIntent(requireContext())) }
            }
        }
        binding.toolbar.btnMyGallery.setOnClickListener {
            triggerAds(AdsTrigger.CONTROLLER_CLICK_GALLERY) {
                runWhenResumed { (activity as? MainActivity)?.showMyGallery() }
            }
        }
        binding.btnEnableController.setOnClickListener {
            triggerAds(AdsTrigger.CONTROLLER_CLICK_ENABLE) {
                runWhenResumed {
                    startActivity(ControllerTutorialActivity.newIntent(requireContext()))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissionState()
        viewModel.refreshActiveTheme()
    }
}
