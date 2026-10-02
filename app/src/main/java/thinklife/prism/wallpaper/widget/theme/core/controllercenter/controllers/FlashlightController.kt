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
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// minSdk=28 >= M(23), no @RequiresApi needed
class FlashlightController @Inject constructor(
    @ApplicationContext context: Context
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId: String? = findFlashCamera()
    private var isOn = false
    var onTorchChanged: ((Boolean) -> Unit)? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(id: String, enabled: Boolean) {
            if (id == cameraId) { isOn = enabled; onTorchChanged?.invoke(enabled) }
        }
        override fun onTorchModeUnavailable(id: String) {
            if (id == cameraId) { isOn = false; onTorchChanged?.invoke(false) }
        }
    }

    init {
        try { cameraManager.registerTorchCallback(torchCallback, null) }
        catch (e: Exception) { Log.e("ControlCenter", "FlashlightController: registerTorchCallback failed", e) }
    }

    private fun findFlashCamera(): String? = try {
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    } catch (e: Exception) { Log.e("ControlCenter", "FlashlightController: findFlashCamera failed", e); null }

    fun toggle() {
        val id = cameraId ?: return
        try { cameraManager.setTorchMode(id, !isOn) }
        catch (e: Exception) { Log.e("ControlCenter", "FlashlightController: toggle failed", e) }
    }

    fun isOn(): Boolean = isOn

    fun release() {
        try { cameraManager.unregisterTorchCallback(torchCallback) }
        catch (e: Exception) { Log.e("ControlCenter", "FlashlightController: release failed", e) }
    }
}
