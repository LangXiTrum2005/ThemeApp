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

package com.ontabb.rate

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Mở trang listing của app trên Play Store. Bê từ `ContextEx.linkAppStore()` của project
 * game-mob-by-dung sang.
 *
 * Đi hai chặng: `market://` để nhảy thẳng vào app Play Store, và nếu máy không có Play Store
 * (emulator, máy Trung Quốc) thì rơi về link https mở bằng trình duyệt.
 *
 * ĐÂY LÀ luồng đánh giá chính: user chấm 4-5 sao trong hộp thoại thì đẩy thẳng ra Play Store
 * để đánh giá ở đó, không dùng In-App Review của Google Play nữa. Cũng dùng cho những chỗ user
 * chủ động đòi mở store (nút "Rate us" trong Settings).
 */
internal fun Context.openPlayStoreListing(storeId: String) {
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$storeId")).apply {
        // NEW_DOCUMENT + MULTIPLE_TASK: mở Play Store thành task riêng, back một phát là về
        // thẳng app chứ không lạc trong lịch sử của Play Store.
        addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK,
        )
        if (this@openPlayStoreListing !is android.app.Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    try {
        Log.d(TAG, "mở Play Store: market://details?id=$storeId")
        startActivity(marketIntent)
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG, "không có app Play Store, rơi về link web", e)
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$storeId"),
        ).apply {
            if (this@openPlayStoreListing !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        runCatching { startActivity(webIntent) }
            .onFailure { Log.w(TAG, "không mở được cả link web", it) }
    }
}

private const val TAG = "OneTabbRate"
