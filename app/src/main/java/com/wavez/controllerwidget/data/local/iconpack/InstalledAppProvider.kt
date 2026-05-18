package com.wavez.controllerwidget.data.local.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.data.model.InstalledApp
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
    suspend fun getInstalledApps(): List<InstalledApp> = withContext(ioDispatcher) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map { ri ->
                InstalledApp(
                    packageName = ri.activityInfo.packageName,
                    appName = ri.loadLabel(pm).toString(),
                    icon = ri.loadIcon(pm),
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    suspend fun getInstalledAppsMap(): Map<String, InstalledApp> =
        getInstalledApps().associateBy { it.packageName }
}
