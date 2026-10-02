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
import android.util.Log

private const val CALCULATOR_TAG = "CalculatorExt"

fun Context.openCalculatorApp(): Boolean {
    val calculatorPackages = listOf(
        "com.google.android.calculator",
        "com.android.calculator2",
        "com.sec.android.app.popupcalculator",
        "com.miui.calculator",
        "com.coloros.calculator",
        "com.oplus.calculator",
        "com.huawei.calculator",
        "com.vivo.calculator",
        "com.transsion.calculator"
    )

    val calculatorIntents = listOf(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR),
        Intent().setComponent(
            ComponentName(
                "com.google.android.calculator",
                "com.android.calculator2.Calculator"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.android.calculator2",
                "com.android.calculator2.Calculator"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.sec.android.app.popupcalculator",
                "com.sec.android.app.popupcalculator.Calculator"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.miui.calculator",
                "com.miui.calculator.cal.CalculatorActivity"
            )
        )
    )

    if (calculatorIntents.any { intent -> startActivitySafely(intent) }) return true

    return calculatorPackages.any { packageName ->
        packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            startActivitySafely(intent)
        } == true
    }
}

private fun Context.startActivitySafely(intent: Intent): Boolean {
    return try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: Exception) {
        Log.d(CALCULATOR_TAG, "Failed to open calculator intent: $intent", e)
        false
    }
}
