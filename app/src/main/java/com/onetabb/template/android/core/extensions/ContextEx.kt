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

package com.onetabb.template.android.core.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import java.io.File
import java.io.InputStream

fun Context.setLocaleSupport(languageTag: String): Boolean = setAppLocale(languageTag)

/**
 * finish() không kèm animation, xử lý cả API mới lẫn cũ:
 * - API 34+: [Activity.overrideActivityTransition] (CLOSE) — phải gọi TRƯỚC finish.
 * - Cũ hơn: overridePendingTransition — phải gọi SAU finish.
 */
fun Activity.finishWithoutAnimation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
        finish()
    } else {
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }
}

fun setAppLocale(languageTag: String): Boolean {
    val normalizedTag = languageTag.trim()
    val locales = if (normalizedTag.isEmpty()) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(normalizedTag)
    }
    if (AppCompatDelegate.getApplicationLocales() == locales) return false

    AppCompatDelegate.setApplicationLocales(locales)
    return true
}

fun Context.openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    } catch (_: Exception) {
    }
}

fun Context.openGooglePlay() {
    val intentUri = "https://play.google.com/store/account/subscriptions".toUri()
    val intent = Intent(Intent.ACTION_VIEW, intentUri)
    startActivity(intent)
}

fun ActivityResultLauncher<String>.launchPermissionIfNeeded(
    activity: Activity,
    permission: String,
    onPermissionRequest: ((Boolean) -> Boolean) = { true },
) {
    if (!activity.shouldShowRequestPermissionRationale(permission)) {
        launch(permission)
        onPermissionRequest(true)
    } else {
        if (onPermissionRequest(false)) activity.openAppSetting()
    }
}

fun Context.openAppSetting() {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    } catch (_: Exception) {
    }
}

fun Context.openNotificationSettings() {
    try {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        }
        startActivity(intent)
    } catch (_: Exception) {
    }
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun Context.redirectApp(redirectUser: String?): Boolean {
    if (redirectUser?.isEmpty() == true) return false
    try {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(redirectUser),
            ),
        )
    } catch (e: Exception) {
        return false
    }
    return true
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Context.openAssetStream(assetPath: String): InputStream = assets.open(assetPath)

fun Context.assetToCacheFile(assetPath: String): File {
    val outFile = File(cacheDir, assetPath.substringAfterLast("/"))
    if (!outFile.exists()) {
        assets.open(assetPath).use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
    return outFile
}

fun Context.openInternetSettings() {
    val intent = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }
    } catch (e: Exception) {
        Intent(Settings.ACTION_WIRELESS_SETTINGS)
    }

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        startActivity(
            Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }
}
