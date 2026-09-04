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
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.runBlocking

data class PermissionStatus(
    val permission: String,
    val granted: Boolean,
)

object AppActivityResultContracts {
    class RequestMultiplePermissions : ActivityResultContract<Array<String>, List<PermissionStatus>>() {

        private val impl = ActivityResultContracts.RequestMultiplePermissions()

        override fun createIntent(
            context: Context,
            input: Array<String>,
        ): Intent = runBlocking {
            for (permission in input) {
                logPermissionPopup(permission)
            }

            impl.createIntent(context, input)
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?,
        ): List<PermissionStatus> = runBlocking {
            impl
                .parseResult(resultCode, intent)
                .also {
                    it.forEach { (permission, granted) ->
                        logPermissionResult(permission, granted)
                    }
                }
                .map { (permission, granted) ->
                    PermissionStatus(
                        permission = permission,
                        granted = granted,
                    )
                }
        }
    }

    class RequestPermission : ActivityResultContract<String, PermissionStatus>() {
        private val impl = RequestMultiplePermissions()
        private var lastRequestedPermission: String? = null

        override fun createIntent(context: Context, input: String): Intent {
            lastRequestedPermission = input
            return impl.createIntent(context, arrayOf(input))
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?,
        ): PermissionStatus = impl.parseResult(resultCode, intent)
            .firstOrNull()
            ?: PermissionStatus(
                permission = lastRequestedPermission ?: "",
                granted = false,
            )
    }
}

private fun logPermissionPopup(permission: String) {
    val analytics = Firebase.analytics

    analytics.logEvent("request_permission") {
        param("permission", permission)
    }

    val eventName = when (permission) {
        Manifest.permission.POST_NOTIFICATIONS -> "noti_permission_view"
        else -> return
    }
    analytics.logEvent(eventName, null)
}

private fun logPermissionResult(permission: String, granted: Boolean) {
    val analytics = Firebase.analytics
    analytics.logEvent("request_permission_result") {
        param("permission", permission)
        param("granted", if (granted) 1 else 0)
    }

    val eventName = when (permission) {
        Manifest.permission.POST_NOTIFICATIONS if granted -> "noti_permission_allow"
        else -> return
    }
    analytics.logEvent(eventName, null)
}
