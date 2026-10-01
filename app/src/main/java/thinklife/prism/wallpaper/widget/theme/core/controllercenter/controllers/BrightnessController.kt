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

package thinklife.prism.wallpaper.widget.theme.core.controllercenter.controllers

import android.content.Context
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToInt

class BrightnessController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun canWriteSettings(): Boolean = Settings.System.canWrite(context)

    fun setBrightness(progress: Int) {
        if (!canWriteSettings()) {
            Log.e("ControlCenter", "BrightnessController: WRITE_SETTINGS not granted")
            return
        }
        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                (progress / 100f * 255).roundToInt().coerceIn(1, 255)
            )
        } catch (e: Exception) {
            Log.e("ControlCenter", "BrightnessController: setBrightness failed", e)
        }
    }

    fun getBrightness(): Int = try {
        val raw = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
        (raw / 255f * 100).roundToInt()
    } catch (e: Exception) {
        Log.e("ControlCenter", "BrightnessController: getBrightness failed", e); 50
    }
}
