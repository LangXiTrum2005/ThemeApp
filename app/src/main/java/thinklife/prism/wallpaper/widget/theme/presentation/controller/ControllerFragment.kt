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

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseFragment
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.ControllerThemeCatalog
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.PermissionHelper
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.service.ControlCenterService
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.databinding.FragmentControllerBinding
import thinklife.prism.wallpaper.widget.theme.presentation.controller.viewmodel.ControllerViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.setting.SettingActivity

@AndroidEntryPoint
class ControllerFragment : BaseFragment<FragmentControllerBinding>() {

    override fun bindingView(): FragmentControllerBinding =
        FragmentControllerBinding.inflate(layoutInflater)

    companion object {
        fun newInstance(): ControllerFragment = ControllerFragment()
    }

    private val viewModel: ControllerViewModel by viewModels()

    private var panelActive = false
    private val themeAdapter = ControllerThemeAdapter { theme ->
        startActivity(ControllerThemePreviewActivity.newIntent(requireContext(), theme.id))
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        binding.toolbar.tvTitle.setText(R.string.controller)
        binding.rvControllerThemes.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = themeAdapter
        }
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        if (viewModel.themesState.value is UiState.Idle) {
            viewModel.loadThemes()
        }
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
            refreshState()
        }
    }

    override fun initListener() {
        super.initListener()
        binding.toolbar.btnSetting.setOnClickListener {
            startActivity(SettingActivity.newIntent(requireContext()))
        }
        binding.toolbar.btnMyGallery.setOnClickListener {
            (activity as? MainActivity)?.showMyGallery()
        }

        binding.btnTogglePanel.setOnClickListener {
            togglePanel()
        }
        binding.tvServiceStatusBadge.setOnClickListener {
            togglePanel()
        }
        binding.cardHero.setOnClickListener {
            togglePanel()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.tvTitle.setText(R.string.controller)
        refreshState()
        viewModel.refreshActiveTheme()
    }

    private fun refreshState() {
        val context = context ?: return

        val accessibilityReady = PermissionHelper.hasAccessibilityEnabled(context)
        if (accessibilityReady) {
            sharedPref.isControllerPermissionCompleted = true
            PermissionHelper.markPermissionCompleted(context)
        }

        // Logic: Chỉ lần đầu xin quyền. Một khi đã hoàn thành hoặc bật quyền thì lưu SharedPreferences và ẩn vĩnh viễn.
        // Chỉ khi user xoá app đi cài lại thì SharedPreferences mới bị xoá và hiển thị lại.
        val isPermissionDone = sharedPref.isControllerPermissionCompleted ||
            PermissionHelper.isPermissionCompleted(context) ||
            accessibilityReady

        if (isPermissionDone) {
            binding.cardHero.isVisible = false
            return
        }

        // Lần đầu chưa cấp quyền: hiển thị card để người dùng bấm cấp quyền
        binding.cardHero.isVisible = true
        binding.tvServiceStatusBadge.setText(R.string.controller_service_inactive)
        binding.tvServiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_inactive)
        binding.tvServiceStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.surfaceDangerHighEm))

        val selectedTheme = sharedPref.activeThemeId?.let { themeId ->
            ControllerThemeCatalog.theme(context, themeId)
        }
        if (selectedTheme != null) {
            binding.tvThemeStatus.text = getString(R.string.controller_theme_applied, selectedTheme.name)
            binding.tvThemeStatus.setTextColor(ContextCompat.getColor(context, R.color.textSuccessHighEm))
            binding.tvThemeStatus.isVisible = true
        } else {
            binding.tvThemeStatus.isVisible = false
        }

        updateTogglePanelButton()
    }

    private fun togglePanel() {
        val context = context ?: return
        if (!PermissionHelper.hasAccessibilityEnabled(context)) {
            PermissionHelper.openAccessibilitySettings(activity ?: context)
            return
        }
        sharedPref.isControllerPermissionCompleted = true
        PermissionHelper.markPermissionCompleted(context)
        panelActive = !panelActive
        context.sendBroadcast(
            Intent(ControlCenterService.ACTION_ACTIVE_CONTROL_PANEL).putExtra(
                ControlCenterService.EXTRA_SHOW, panelActive
            )
        )
        refreshState()
    }

    private fun updateTogglePanelButton() {
        binding.btnTogglePanel.text = if (panelActive) {
            getString(R.string.hide_control_panel)
        } else {
            getString(R.string.test_control_panel)
        }
    }

}
