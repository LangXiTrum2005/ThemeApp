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

package thinklife.prism.wallpaper.widget.theme.core.controllercenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.service.ControlCenterService

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
