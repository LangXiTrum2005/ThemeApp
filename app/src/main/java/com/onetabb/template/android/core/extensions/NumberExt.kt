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

import java.text.DateFormat
import java.util.Locale

fun convertTimeToHourMinuteSecond(time: Long): String {
    val secondTime = time / 1000
    val seconds = secondTime % 60
    val minutes = ((secondTime - seconds) / 60) % 60
    val hours = (secondTime - seconds - minutes * 60) / (60 * 60)
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

/** Định dạng tốc độ byte/giây thành chuỗi đọc được, vd "1.2 MB/s". */
fun formatSpeed(bytesPerSec: Long): String {
    val bps = if (bytesPerSec < 0) 0 else bytesPerSec
    return when {
        bps < 1024 -> String.format(Locale.US, "%d B/s", bps)
        bps < 1024 * 1024 -> String.format(Locale.US, "%.1f KB/s", bps / 1024.0)
        else -> String.format(Locale.US, "%.1f MB/s", bps / (1024.0 * 1024.0))
    }
}

fun convertTimeToDate(strTime: String): String {
    try {
        val time = strTime.toLong()
        return DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
            Locale.getDefault(),
        ).format(time)
    } catch (_: Exception) {
        return ""
    }
}
