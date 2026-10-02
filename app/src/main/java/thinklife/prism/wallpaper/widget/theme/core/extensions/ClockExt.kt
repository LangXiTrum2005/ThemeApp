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

package thinklife.prism.wallpaper.widget.theme.core.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log

private const val TAG = "ClockExt"

fun Context.openStopwatchApp(): Boolean {
    val clockPackages = listOf(
        "com.google.android.deskclock",
        "com.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.coloros.alarmclock",
        "com.oplus.alarmclock",
        "com.miui.clock",
        "com.huawei.deskclock"
    )

    val stopwatchIntents = listOf(
        Intent("com.google.android.deskclock.action.SHOW_STOPWATCH")
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setPackage("com.google.android.deskclock"),
        Intent("com.google.android.deskclock.action.SHOW_STOPWATCH")
            .setComponent(
                ComponentName(
                    "com.google.android.deskclock",
                    "com.android.deskclock.DeskClock"
                )
            ),
        Intent("com.android.deskclock.action.SHOW_STOPWATCH")
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setPackage("com.android.deskclock"),
        Intent("com.android.deskclock.action.SHOW_STOPWATCH")
            .setComponent(
                ComponentName(
                    "com.android.deskclock",
                    "com.android.deskclock.DeskClock"
                )
            ),
        Intent("android.intent.action.SHOW_STOPWATCH"),
        Intent("com.sec.android.app.clockpackage.STOPWATCH")
            .setPackage("com.sec.android.app.clockpackage"),
        Intent().setComponent(
            ComponentName(
                "com.sec.android.app.clockpackage",
                "com.sec.android.app.clockpackage.stopwatch.Stopwatch"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.sec.android.app.clockpackage",
                "com.sec.android.app.clockpackage.stopwatch.viewmodel.StopwatchActivity"
            )
        ),
        Intent(Intent.ACTION_MAIN)
            .setPackage("com.android.deskclock")
            .putExtra("deskclock.select.tab", 3)
            .putExtra("selected_tab", 3),
        Intent(Intent.ACTION_MAIN)
            .setPackage("com.google.android.deskclock")
            .putExtra("deskclock.select.tab", 3)
            .putExtra("selected_tab", 3),
        Intent(Intent.ACTION_MAIN)
            .setPackage("com.miui.clock")
            .putExtra("deskclock.select.tab", 3)
            .putExtra("selected_tab", 3),
        Intent(AlarmClock.ACTION_SHOW_TIMERS),
        Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_CLOCK")
    )

    if (stopwatchIntents.any { intent -> startActivitySafely(intent) }) return true

    return clockPackages.any { packageName ->
        packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            startActivitySafely(intent)
        } == true
    } || startActivitySafely(Intent(AlarmClock.ACTION_SHOW_ALARMS))
}

private fun Context.startActivitySafely(intent: Intent): Boolean {
    return try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: Exception) {
        Log.d(TAG, "Failed to open clock intent: $intent", e)
        false
    }
}
