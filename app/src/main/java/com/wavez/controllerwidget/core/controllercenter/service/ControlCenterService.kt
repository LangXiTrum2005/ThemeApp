package com.wavez.controllerwidget.core.controllercenter.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.wavez.controllerwidget.core.controllercenter.controllers.BluetoothController
import com.wavez.controllerwidget.core.controllercenter.controllers.BrightnessController
import com.wavez.controllerwidget.core.controllercenter.controllers.FlashlightController
import com.wavez.controllerwidget.core.controllercenter.controllers.MediaController
import com.wavez.controllerwidget.core.controllercenter.controllers.NetworkController
import com.wavez.controllerwidget.core.controllercenter.controllers.VolumeController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ControlCenterService : AccessibilityService() {

    companion object {
        const val ACTION_ACTIVE_CONTROL_PANEL =
            "com.wavez.themewallpaper.controlcenter.ACTION_ACTIVE_CONTROL_PANEL"
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

    private var overlay: ControlCenterOverlay? = null

    private val panelToggleReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ACTION_ACTIVE_CONTROL_PANEL) return
            val show = intent.getBooleanExtra(EXTRA_SHOW, false)
            Log.d(TAG, "Broadcast received: show=$show")
            if (show) overlay?.show() else overlay?.hide()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected — service running")
        try {
            registerReceiver(
                panelToggleReceiver,
                IntentFilter(ACTION_ACTIVE_CONTROL_PANEL),
                RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {
            Log.e(TAG, "Failed to register receiver")
        }

        overlay = ControlCenterOverlay(this)
        overlay?.init()
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
        runCatching { unregisterReceiver(panelToggleReceiver) }
        overlay?.destroy()
        runCatching {
            volumeController.release()
            networkController.release()
            bluetoothController.release()
            flashlightController.release()
            mediaController.release()
        }
    }
}
