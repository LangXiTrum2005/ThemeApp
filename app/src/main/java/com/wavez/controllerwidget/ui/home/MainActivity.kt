package com.wavez.controllerwidget.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.controllercenter.PermissionHelper
import com.wavez.controllerwidget.core.controllercenter.RequiredPermission
import com.wavez.controllerwidget.core.controllercenter.ThemeLoader
import com.wavez.controllerwidget.core.controllercenter.model.ControllerConfigModel
import com.wavez.controllerwidget.core.controllercenter.service.ControlCenterService
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun bindingView(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    companion object {
        fun newIntent(activity: Activity): Intent {
            return Intent(activity, MainActivity::class.java)
        }
    }

    private val viewModel by viewModels<MainViewModel>()

    private var panelActive = false

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
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* no-op */ }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        observeViewModel()
        viewModel.getWallpapers()
        binding.togglePanelButton.setOnClickListener { togglePanel() }
        requestPermissions.launch(runtimePermissions.toTypedArray())
    }

    private fun observeViewModel() {
        viewModel.wallpapersState.collectWithLifecycle(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> {
                    // Show loading view here.
                }

                is UiState.Data -> {
                    // Bind state.value to RecyclerView/adapter here.
                }

                is UiState.Error -> {
                    // Show state.message to user here.
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        renderHomeState()
    }

    private fun renderHomeState() {
        val missingSpecial = PermissionHelper.getMissingPermissions(this)
        val accessibilityReady = missingSpecial.none { it == RequiredPermission.ACCESSIBILITY }
        val themeReady = ThemeLoader.isReady(this)
        val missingCount = ThemeLoader.missingAssets(this).size

        binding.accessibilityStatusText.apply {
            text = if (accessibilityReady) {
                setTextColor(Color.rgb(56, 142, 60))
                "Accessibility: ENABLED"
            } else {
                setTextColor(Color.rgb(176, 0, 32))
                "Accessibility: NOT ENABLED"
            }
        }

        binding.themeStatusText.apply {
            text = if (themeReady) {
                setTextColor(Color.rgb(56, 142, 60))
                "Theme: READY"
            } else {
                setTextColor(Color.rgb(230, 81, 0))
                "Theme: $missingCount/${ControllerConfigModel.Companion.REQUIRED_ASSETS.size} files missing - copy assets to filesDir/theme/theme_1/"
            }
        }

        binding.togglePanelButton.isEnabled = accessibilityReady
        binding.permissionButtonsContainer.removeAllViews()
        missingSpecial.forEach { permission ->
            binding.permissionButtonsContainer.addView(createPermissionButton(permission))
        }
    }

    private fun togglePanel() {
        panelActive = !panelActive
        sendBroadcast(
            Intent(ControlCenterService.Companion.ACTION_ACTIVE_CONTROL_PANEL).putExtra(
                ControlCenterService.Companion.EXTRA_SHOW, panelActive
            )
        )
        binding.togglePanelButton.text = if (panelActive) "Hide Panel" else "Show Panel"
    }

    private fun createPermissionButton(permission: RequiredPermission): Button {
        return Button(this).apply {
            text = when (permission) {
                RequiredPermission.ACCESSIBILITY -> "Enable Accessibility Service"
                RequiredPermission.WRITE_SETTINGS -> "Grant Write Settings"
                RequiredPermission.NOTIFICATION_ACCESS -> "Grant Notification Access"
            }
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(230, 81, 0))
            setOnClickListener {
                when (permission) {
                    RequiredPermission.ACCESSIBILITY -> PermissionHelper.openAccessibilitySettings(
                        this@MainActivity
                    )

                    RequiredPermission.WRITE_SETTINGS -> PermissionHelper.openWriteSettingsPage(this@MainActivity)
                    RequiredPermission.NOTIFICATION_ACCESS -> PermissionHelper.openNotificationAccessSettings(
                        this@MainActivity
                    )
                }
            }
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 4.dp
                bottomMargin = 4.dp
            }
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
