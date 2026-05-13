package com.wavez.themewallpaper.core.controllercenter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import com.wavez.themewallpaper.core.controllercenter.service.ControlCenterService
import com.wavez.themewallpaper.core.controllercenter.service.ControlNotificationListener
import androidx.core.net.toUri

enum class RequiredPermission {
     ACCESSIBILITY, WRITE_SETTINGS, NOTIFICATION_ACCESS
}

object PermissionHelper {

    fun hasAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (!am.isEnabled) return false
        val flat = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val expected = ComponentName(context, ControlCenterService::class.java).flattenToString()
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(flat)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    fun hasWriteSettings(context: Context): Boolean =
        Settings.System.canWrite(context)

    fun hasNotificationAccess(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        val expected = ComponentName(context, ControlNotificationListener::class.java).flattenToString()
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(flat)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    fun openOverlaySettings(context: Context) =
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )

    fun openAccessibilitySettings(context: Context) =
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )

    fun openWriteSettingsPage(context: Context) =
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, "package:${context.packageName}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )

    fun openNotificationAccessSettings(context: Context) =
        context.startActivity(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )

    fun getMissingPermissions(context: Context): List<RequiredPermission> = buildList {
        if (!hasAccessibilityEnabled(context)) add(RequiredPermission.ACCESSIBILITY)
        if (!hasWriteSettings(context)) add(RequiredPermission.WRITE_SETTINGS)
        if (!hasNotificationAccess(context)) add(RequiredPermission.NOTIFICATION_ACCESS)
    }
}
