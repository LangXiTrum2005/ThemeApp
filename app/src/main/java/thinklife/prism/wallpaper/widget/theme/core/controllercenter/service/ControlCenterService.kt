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

package thinklife.prism.wallpaper.widget.theme.core.controllercenter.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.ThemeLoader
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.controllers.BluetoothController
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.controllers.BrightnessController
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.controllers.FlashlightController
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.controllers.MediaController
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.controllers.NetworkController
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.controllers.VolumeController
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerConfigModel
import thinklife.prism.wallpaper.widget.theme.data.local.controller.ControllerFilePathProvider
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPref
import thinklife.prism.wallpaper.widget.theme.data.repository.ThemeControllerRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ControlCenterService : AccessibilityService() {

    companion object {
        const val ACTION_ACTIVE_CONTROL_PANEL =
            "thinklife.prism.wallpaper.widget.theme.controlcenter.ACTION_ACTIVE_CONTROL_PANEL"
        const val ACTION_RELOAD_THEME =
            "thinklife.prism.wallpaper.widget.theme.controlcenter.ACTION_RELOAD_THEME"
        const val EXTRA_SHOW = "extra_show"
        private const val TAG = "ControlCenter"
    }

    @Inject
    lateinit var volumeController: VolumeController
    @Inject
    lateinit var brightnessController: BrightnessController
    @Inject
    lateinit var networkController: NetworkController
    @Inject
    lateinit var bluetoothController: BluetoothController
    @Inject
    lateinit var flashlightController: FlashlightController
    @Inject
    lateinit var mediaController: MediaController
    @Inject
    lateinit var sharedPref: SharedPref
    @Inject
    lateinit var themeFilePathProvider: ControllerFilePathProvider
    @Inject
    lateinit var controllerRepository: ThemeControllerRepository

    private var overlay: ControlCenterOverlay? = null

    private val controlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_ACTIVE_CONTROL_PANEL -> {
                    val show = intent.getBooleanExtra(EXTRA_SHOW, false)
                    Log.d(TAG, "Panel toggle: show=$show")
                    if (show) overlay?.show() else overlay?.hide()
                }

                ACTION_RELOAD_THEME -> {
                    Log.d(TAG, "Theme reload requested")
                    overlay?.reload(loadActiveTheme())
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected — service running")

        try {
            val filter = IntentFilter().apply {
                addAction(ACTION_ACTIVE_CONTROL_PANEL)
                addAction(ACTION_RELOAD_THEME)
            }
            ContextCompat.registerReceiver(
                this,
                controlReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
        } catch (_: Exception) {
            Log.e(TAG, "Failed to register receiver")
        }

        overlay = ControlCenterOverlay(this)
        overlay?.init(loadActiveTheme())
        overlay?.bindControllers(
            network = networkController,
            bluetooth = bluetoothController,
            flashlight = flashlightController,
            media = mediaController,
            volume = volumeController,
            brightness = brightnessController
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(controlReceiver) }
        overlay?.destroy()
        runCatching {
            volumeController.release()
            networkController.release()
            bluetoothController.release()
            flashlightController.release()
            mediaController.release()
        }
    }

    // Loads the theme the user last applied, falling back to the built-in default if:
    // - No theme has been applied yet (activeThemeId is null)
    // - The extracted directory is missing or empty (e.g., storage was cleared)
    private fun loadActiveTheme(): ControllerConfigModel {
        val themeId = sharedPref.activeThemeId
        if (themeId != null) {
            controllerRepository.getThemeConfig(themeId)?.let { return it }
            val dir = themeFilePathProvider.getExtractedThemeDir(themeId)
            if (dir.isDirectory && dir.listFiles()?.isNotEmpty() == true) {
                return ThemeLoader.loadFromDirectory(dir, themeId)
            }
            Log.w(TAG, "Active theme dir missing for $themeId, falling back to default")
        }
        return ThemeLoader.load(this)
    }
}
