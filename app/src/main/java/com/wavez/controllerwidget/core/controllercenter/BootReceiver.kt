package com.wavez.controllerwidget.core.controllercenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wavez.controllerwidget.core.controllercenter.service.ControlCenterService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = context.getSharedPreferences("control_center_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("service_enabled", false)) {
            Log.d("ControlCenter", "BootReceiver: service not enabled, skipping")
            return
        }

        try {
            context.startForegroundService(Intent(context, ControlCenterService::class.java))
            Log.d("ControlCenter", "BootReceiver: service started")
        } catch (e: Exception) {
            Log.e("ControlCenter", "BootReceiver: failed to start service", e)
        }
    }
}
