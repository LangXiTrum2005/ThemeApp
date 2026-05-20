package com.wavez.controllerwidget.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val pm = context.packageManager

    @Volatile private var cachedApps: List<AppInfo>? = null
    @Volatile private var cacheTimestamp = 0L
    private val cacheLock = Any()

    companion object {
        private const val CACHE_TTL_MS = 10 * 60_000L
    }

    suspend fun getInstalledApps(): List<AppInfo> =
        withContext(ioDispatcher) { getFromCacheOrQuery() }

    fun invalidate() = clearCache()

    private fun getFromCacheOrQuery(): List<AppInfo> {
        val now = System.currentTimeMillis()
        cachedApps?.let { if (now - cacheTimestamp < CACHE_TTL_MS) return it }

        return synchronized(cacheLock) {
            // double-checked: another thread may have populated while we waited
            cachedApps?.let { if (now - cacheTimestamp < CACHE_TTL_MS) return it }

            buildAppList().also {
                cachedApps = it
                cacheTimestamp = now
            }
        }
    }

    private fun buildAppList(): List<AppInfo> =
        getLaunchableApps()
            .map { info ->
                AppInfo(
                    packageName = info.packageName,
                    appName = pm.getApplicationLabel(info).toString(),
                    icon = pm.getApplicationIcon(info.packageName),
                )
            }
            .sortedBy { it.appName.lowercase() }

    private fun getLaunchableApps(): List<ApplicationInfo> {
        val myPackage = context.packageName
        return getInstalledApplications().filter { info ->
            info.packageName != myPackage
                && pm.getLaunchIntentForPackage(info.packageName) != null
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getInstalledApplications(): List<ApplicationInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
    }

    private fun clearCache() = synchronized(cacheLock) {
        cachedApps = null
        cacheTimestamp = 0L
    }
}
