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

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import androidx.core.net.toUri
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.service.ControlCenterService
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.service.ControlNotificationListener

enum class RequiredPermission {
    ACCESSIBILITY, WRITE_SETTINGS, NOTIFICATION_ACCESS
}

object PermissionHelper {

    private const val PREFS_NAME = "shared_preferences_app_data"
    private const val KEY_WAITING_ACCESSIBILITY_TIME = "key_waiting_accessibility_time"
    private const val KEY_WAITING_ACCESSIBILITY_ACTIVITY = "key_waiting_accessibility_activity"
    private const val KEY_CONTROLLER_PERMISSION_COMPLETED = "key_controller_permission_completed"
    private const val ACCESSIBILITY_TIMEOUT_MS = 180_000L // 3 minutes

    fun markPermissionCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CONTROLLER_PERMISSION_COMPLETED, true).apply()
    }

    fun isPermissionCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_CONTROLLER_PERMISSION_COMPLETED, false)
    }

    fun markWaitingForAccessibility(context: Context) {
        val activityName = (context as? android.app.Activity)?.javaClass?.name
            ?: (context as? android.content.ContextWrapper)?.baseContext?.let { (it as? android.app.Activity)?.javaClass?.name }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putLong(KEY_WAITING_ACCESSIBILITY_TIME, System.currentTimeMillis())
            if (activityName != null) {
                putString(KEY_WAITING_ACCESSIBILITY_ACTIVITY, activityName)
            } else {
                remove(KEY_WAITING_ACCESSIBILITY_ACTIVITY)
            }
            apply()
        }
    }

    fun isWaitingForAccessibility(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val time = prefs.getLong(KEY_WAITING_ACCESSIBILITY_TIME, 0L)
        return (System.currentTimeMillis() - time) in 1..ACCESSIBILITY_TIMEOUT_MS
    }

    fun getTargetActivityClass(context: Context): Class<*> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val className = prefs.getString(KEY_WAITING_ACCESSIBILITY_ACTIVITY, null)
        if (!className.isNullOrBlank()) {
            try {
                return Class.forName(className)
            } catch (_: Exception) {
            }
        }
        return thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity::class.java
    }

    fun clearWaitingForAccessibility(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_WAITING_ACCESSIBILITY_TIME)
            .remove(KEY_WAITING_ACCESSIBILITY_ACTIVITY)
            .apply()
    }

    fun hasAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        if (am != null && am.isEnabled) {
            val enabledServices = runCatching {
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            }.getOrNull()
            if (enabledServices != null) {
                for (service in enabledServices) {
                    val serviceInfo = service.resolveInfo?.serviceInfo ?: continue
                    if (serviceInfo.packageName.equals(context.packageName, ignoreCase = true) &&
                        serviceInfo.name.contains("ControlCenterService", ignoreCase = true)
                    ) {
                        return true
                    }
                }
            }
        }

        val flat = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

        val expectedLong = ComponentName(context, ControlCenterService::class.java).flattenToString()
        val expectedShort = ComponentName(context, ControlCenterService::class.java).flattenToShortString()
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(flat)
        while (splitter.hasNext()) {
            val component = splitter.next()
            if (component.equals(expectedLong, ignoreCase = true) ||
                component.equals(expectedShort, ignoreCase = true) ||
                (component.contains(context.packageName, ignoreCase = true) && component.contains("ControlCenterService", ignoreCase = true))
            ) {
                return true
            }
        }
        return false
    }

    fun hasWriteSettings(context: Context): Boolean =
        Settings.System.canWrite(context)

    fun hasNotificationAccess(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false

        val expectedLong = ComponentName(context, ControlNotificationListener::class.java).flattenToString()
        val expectedShort = ComponentName(context, ControlNotificationListener::class.java).flattenToShortString()
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(flat)
        while (splitter.hasNext()) {
            val component = splitter.next()
            if (component.equals(expectedLong, ignoreCase = true) ||
                component.equals(expectedShort, ignoreCase = true) ||
                (component.contains(context.packageName, ignoreCase = true) && component.contains("ControlNotificationListener", ignoreCase = true))
            ) {
                return true
            }
        }
        return false
    }

    fun openOverlaySettings(context: Context) =
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri(),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )

    fun openAccessibilitySettings(context: Context) {
        markWaitingForAccessibility(context)
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    fun openWriteSettingsPage(context: Context) =
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, "package:${context.packageName}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )

    fun openNotificationAccessSettings(context: Context) =
        context.startActivity(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )

    fun getMissingPermissions(context: Context): List<RequiredPermission> = buildList {
        if (!hasAccessibilityEnabled(context)) add(RequiredPermission.ACCESSIBILITY)
        if (!hasWriteSettings(context)) add(RequiredPermission.WRITE_SETTINGS)
        if (!hasNotificationAccess(context)) add(RequiredPermission.NOTIFICATION_ACCESS)
    }
}
