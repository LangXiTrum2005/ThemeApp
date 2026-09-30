package com.wavez.themewallpaper.ui.home.fragment.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.base.BaseFragment
import com.wavez.themewallpaper.core.controllercenter.PermissionHelper
import com.wavez.themewallpaper.core.controllercenter.ControllerThemeCatalog
import com.wavez.themewallpaper.core.controllercenter.ThemeLoader
import com.wavez.themewallpaper.core.controllercenter.service.ControlCenterService
import com.wavez.themewallpaper.databinding.FragmentControllerBinding
import com.wavez.themewallpaper.ui.home.MainActivity
import com.wavez.themewallpaper.ui.setting.SettingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ControllerFragment : BaseFragment<FragmentControllerBinding>() {

    override fun bindingView(): FragmentControllerBinding =
        FragmentControllerBinding.inflate(layoutInflater)

    companion object {
        fun newInstance(): ControllerFragment = ControllerFragment()
    }

    private var panelActive = false
    private val themeAdapter = ControllerThemeAdapter { theme ->
        startActivity(ControllerThemePreviewActivity.newIntent(requireContext(), theme.id))
    }

    private val runtimePermissions = buildList {
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            refreshState()
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
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val themes = withContext(Dispatchers.IO) {
                ControllerThemeCatalog.themes(appContext)
            }
            themeAdapter.submit(themes, sharedPref.activeThemeId)
            binding.rvControllerThemes.scheduleLayoutAnimation()
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

        binding.btnGrantAccessibility.setOnClickListener {
            PermissionHelper.openAccessibilitySettings(requireContext())
        }

        binding.btnGrantWriteSettings.setOnClickListener {
            PermissionHelper.openWriteSettingsPage(requireContext())
        }

        binding.btnGrantNotification.setOnClickListener {
            PermissionHelper.openNotificationAccessSettings(requireContext())
        }

        binding.btnGrantRuntime.setOnClickListener {
            requestPermissions.launch(runtimePermissions.toTypedArray())
        }
    }

    override fun onResume() {
        super.onResume()
        refreshState()
        sharedPref.activeThemeId?.let { id ->
            ControllerThemeCatalog.theme(requireContext(), id)?.let(themeAdapter::markApplied)
        }
    }

    private fun refreshState() {
        val context = context ?: return

        // 1. Accessibility Service status
        val accessibilityReady = PermissionHelper.hasAccessibilityEnabled(context)
        binding.btnGrantAccessibility.isVisible = !accessibilityReady
        binding.tvAccessibilityGranted.isVisible = accessibilityReady

        if (accessibilityReady) {
            binding.tvServiceStatusBadge.text = "ACTIVE"
            binding.tvServiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_active)
            binding.tvServiceStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.textSuccessHighEm))
            binding.btnTogglePanel.isEnabled = true
            binding.btnTogglePanel.alpha = 1.0f
        } else {
            binding.tvServiceStatusBadge.text = "INACTIVE"
            binding.tvServiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_inactive)
            binding.tvServiceStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.surfaceDangerHighEm))
            binding.btnTogglePanel.isEnabled = false
            binding.btnTogglePanel.alpha = 0.5f
        }

        // 2. Write Settings permission
        val writeSettingsReady = PermissionHelper.hasWriteSettings(context)
        binding.btnGrantWriteSettings.isVisible = !writeSettingsReady
        binding.tvWriteSettingsGranted.isVisible = writeSettingsReady

        // 3. Notification Access
        val notificationReady = PermissionHelper.hasNotificationAccess(context)
        binding.btnGrantNotification.isVisible = !notificationReady
        binding.tvNotificationGranted.isVisible = notificationReady

        // 4. Runtime permissions
        val runtimeGranted = runtimePermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        binding.btnGrantRuntime.isVisible = !runtimeGranted
        binding.tvRuntimeGranted.isVisible = runtimeGranted

        val allControllerPermissionsGranted =
            accessibilityReady && writeSettingsReady && notificationReady && runtimeGranted
        binding.tvPermissionsTitle.isVisible = !allControllerPermissionsGranted
        binding.cardPermissions.isVisible = !allControllerPermissionsGranted
        binding.tvControllerThemesTitle.isVisible = false
        binding.rvControllerThemes.isVisible = allControllerPermissionsGranted

        // 5. Theme Status
        val selectedTheme = sharedPref.activeThemeId?.let { themeId ->
            ControllerThemeCatalog.theme(context, themeId)
        }
        val themeReady = selectedTheme != null || ThemeLoader.isReady(context)
        val missingCount = ThemeLoader.missingAssets(context).size
        if (selectedTheme != null) {
            binding.tvThemeStatus.text = getString(R.string.controller_theme_applied, selectedTheme.name)
            binding.tvThemeStatus.setTextColor(ContextCompat.getColor(context, R.color.textSuccessHighEm))
        } else if (themeReady) {
            binding.tvThemeStatus.text = "Theme Assets: Ready"
            binding.tvThemeStatus.setTextColor(ContextCompat.getColor(context, R.color.textSuccessHighEm))
        } else {
            binding.tvThemeStatus.text = "Theme Assets: $missingCount/${ThemeLoader.REQUIRED_ASSETS.size} missing"
            binding.tvThemeStatus.setTextColor(ContextCompat.getColor(context, R.color.textMedEm))
        }

        updateTogglePanelButton()
    }

    private fun togglePanel() {
        val context = context ?: return
        panelActive = !panelActive
        context.sendBroadcast(
            Intent(ControlCenterService.ACTION_ACTIVE_CONTROL_PANEL).putExtra(
                ControlCenterService.EXTRA_SHOW, panelActive
            )
        )
        updateTogglePanelButton()
    }

    private fun updateTogglePanelButton() {
        binding.btnTogglePanel.text = if (panelActive) {
            getString(R.string.hide_control_panel)
        } else {
            getString(R.string.test_control_panel)
        }
    }

}
