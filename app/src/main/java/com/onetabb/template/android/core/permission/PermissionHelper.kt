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

package com.onetabb.template.android.core.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.onetabb.template.android.core.extensions.openAppSetting
import com.onetabb.template.android.core.extensions.openNotificationSettings

/**
 * Trả lời một câu duy nhất: lần này bấm xin quyền thì hệ thống CÓ hiện hộp thoại không, hay phải
 * đẩy user sang Settings.
 *
 * Android không có API hỏi thẳng điều đó. `shouldShowRequestPermissionRationale` trả `false` cho
 * CẢ HAI trường hợp trái ngược nhau: chưa hỏi lần nào, và đã bị từ chối vĩnh viễn. Phân biệt được
 * hai cái đó chỉ còn cách tự nhớ "app đã từng xin quyền này chưa" — đó là việc của [markAsRequested].
 *
 * Luồng chuẩn ở nơi gọi:
 *
 * ```kotlin
 * private val launcher = registerRequestNotiPermissionResult { status ->
 *     PermissionHelper.markAsRequested(this, Manifest.permission.POST_NOTIFICATIONS)
 *     onPermissionResult(status.granted)
 * }
 *
 * private fun askNotificationPermission() {
 *     val permission = Manifest.permission.POST_NOTIFICATIONS
 *     when {
 *         PermissionHelper.isGranted(this, permission) -> goNext()
 *         // Hệ thống không hiện hộp thoại nữa -> giải thích rồi mở Settings.
 *         PermissionHelper.mustOpenSettings(this, permission) -> PermissionDialog.show(
 *             supportFragmentManager,
 *             PermissionDialogArgs(
 *                 icon = R.drawable.ic_permission_notification,
 *                 title = R.string.permission_notification_settings_title,
 *                 description = R.string.permission_notification_settings_description,
 *                 positiveText = R.string.permission_open_settings,
 *             ),
 *         )
 *         else -> launcher.launch()
 *     }
 * }
 * ```
 *
 * Mở Settings xong app xuống background, nên nơi gọi phải kiểm tra lại trạng thái ở `onResume`
 * chứ không có callback nào báo về.
 */
object PermissionHelper {

    private const val PREFS_NAME = "permission_prefs"
    private const val KEY_PREFIX_REQUESTED = "requested_"

    /** Quyền đã được cấp chưa. Với quyền chỉ có từ API mới, máy cũ coi như đã có. */
    fun isGranted(context: Context, permission: String): Boolean = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * Đánh dấu app VỪA bật hộp thoại quyền của hệ thống. BẮT BUỘC gọi trong callback của
     * launcher, nếu không [mustOpenSettings] sẽ mãi trả `false` và user kẹt ở nút không phản hồi.
     */
    fun markAsRequested(context: Context, permission: String) {
        context.permissionPrefs().edit { putBoolean(KEY_PREFIX_REQUESTED + permission, true) }
    }

    /** App đã từng bật hộp thoại quyền này lần nào chưa. */
    fun hasRequestedBefore(context: Context, permission: String): Boolean = context.permissionPrefs().getBoolean(KEY_PREFIX_REQUESTED + permission, false)

    /**
     * `true` khi hệ thống sẽ KHÔNG hiện hộp thoại nữa (user đã chọn "Don't allow" tới mức chặn
     * vĩnh viễn) — lúc đó gọi launcher là vô ích, phải đưa user sang Settings bằng [openSettings].
     *
     * Quyền đã cấp rồi thì trả `false`: không có gì để xin.
     */
    fun mustOpenSettings(activity: Activity, permission: String): Boolean = when {
        isGranted(activity, permission) -> false
        !hasRequestedBefore(activity, permission) -> false
        else -> !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Mở đúng trang Settings cho quyền đó: quyền thông báo đi thẳng vào trang thông báo của app,
     * còn lại về trang thông tin ứng dụng.
     */
    fun openSettings(context: Context, permission: String) {
        if (permission == NOTIFICATION_PERMISSION) {
            context.openNotificationSettings()
        } else {
            context.openAppSetting()
        }
    }

    /**
     * Thông báo có đang bật không. Khác [isGranted]: máy dưới API 33 không có quyền runtime nào
     * cho thông báo, nhưng user vẫn tắt được trong Settings — chỗ duy nhất biết là API này.
     */
    fun areNotificationsEnabled(context: Context): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** Máy có cần xin quyền thông báo lúc chạy không (chỉ Android 13 trở lên). */
    fun isNotificationPermissionRequired(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @Suppress("InlinedApi") // hằng số chỉ là chuỗi, đọc được ở mọi API level
    const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS

    private fun Context.permissionPrefs() = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
