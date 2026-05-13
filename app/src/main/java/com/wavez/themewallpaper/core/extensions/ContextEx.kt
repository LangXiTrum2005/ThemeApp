package com.wavez.themewallpaper.core.extensions

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import java.io.File
import java.io.InputStream
import java.util.Locale

fun Context.setLocaleSupport(c: String) {
    val ca = c.split("-")
    val locale = Locale(ca.firstOrNull() ?: return, ca.getOrNull(1) ?: "")
    Locale.setDefault(locale)
    val tag = locale.toLanguageTag()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSystemService(LocaleManager::class.java).applicationLocales =
            LocaleList.forLanguageTags(tag)
    } else {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(tag)
        )
    }
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
    activity: Activity, permission: String, onPermissionRequest: ((Boolean) -> Boolean) = { true }
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
                Intent.ACTION_VIEW, Uri.parse(redirectUser)
            )
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

fun Context.openAssetStream(assetPath: String): InputStream {
    return assets.open(assetPath)
}

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
