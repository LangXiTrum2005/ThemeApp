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

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class NotificationEvent(
    val key: String,
    val appName: String,
    val title: String,
    val text: String,
    val isRemoval: Boolean
)

object NotificationBus {
    private val _events = MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<NotificationEvent> = _events.asSharedFlow()

    fun emit(event: NotificationEvent) {
        _events.tryEmit(event)
    }
}

class ControlNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        sendMediaAccessChanged(ACTION_NOTIFICATION_LISTENER_CONNECTED)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        sendMediaAccessChanged(ACTION_NOTIFICATION_LISTENER_DISCONNECTED)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val pm = applicationContext.packageManager
            val appName = try {
                pm.getApplicationLabel(pm.getApplicationInfo(sbn.packageName, 0)).toString()
            } catch (e: Exception) {
                sbn.packageName
            }
            NotificationBus.emit(
                NotificationEvent(key = sbn.key, appName = appName, title = title, text = text, isRemoval = false)
            )
        } catch (e: Exception) {
            Log.e("ControlCenter", "ControlNotificationListener: onNotificationPosted failed", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        try {
            NotificationBus.emit(
                NotificationEvent(key = sbn.key, appName = "", title = "", text = "", isRemoval = true)
            )
        } catch (e: Exception) {
            Log.e("ControlCenter", "ControlNotificationListener: onNotificationRemoved failed", e)
        }
    }

    private fun sendMediaAccessChanged(action: String) {
        sendBroadcast(Intent(action).setPackage(packageName))
    }

    companion object {
        const val ACTION_NOTIFICATION_LISTENER_CONNECTED =
            "thinklife.prism.wallpaper.widget.theme.controlcenter.NOTIFICATION_LISTENER_CONNECTED"
        const val ACTION_NOTIFICATION_LISTENER_DISCONNECTED =
            "thinklife.prism.wallpaper.widget.theme.controlcenter.NOTIFICATION_LISTENER_DISCONNECTED"
    }
}
