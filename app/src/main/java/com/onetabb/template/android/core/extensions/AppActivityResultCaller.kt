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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat

fun ActivityResultCaller.registerRequestNotiPermissionResult(
    callback: ActivityResultCallback<PermissionStatus>,
): ActivityResultLauncher<Unit> {
    val contract = AppActivityResultContracts.RequestPermission()
    val resultLauncher = registerForActivityResult(contract, callback)
    return RequestNotiPermissionLauncher(resultLauncher, contract, callback)
}

private class RequestNotiPermissionLauncher(
    private val launcher: ActivityResultLauncher<String>,
    private val callerContract: AppActivityResultContracts.RequestPermission,
    private val callback: ActivityResultCallback<PermissionStatus>,
) : ActivityResultLauncher<Unit>() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val callerInput = Manifest.permission.POST_NOTIFICATIONS

    private val resultContract: ActivityResultContract<Unit, PermissionStatus> by lazy {
        object : ActivityResultContract<Unit, PermissionStatus>() {
            @SuppressLint("NewApi")
            override fun createIntent(context: Context, input: Unit): Intent = callerContract.createIntent(context, callerInput)

            override fun parseResult(resultCode: Int, intent: Intent?): PermissionStatus = callerContract.parseResult(resultCode, intent)
        }
    }

    override fun launch(input: Unit, options: ActivityOptionsCompat?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(callerInput, options)
        } else {
            @SuppressLint("NewApi")
            callback.onActivityResult(PermissionStatus(callerInput, true))
        }
    }

    override fun unregister() {
        launcher.unregister()
    }

    override val contract: ActivityResultContract<Unit, *> = resultContract
}
