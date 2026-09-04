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

import android.content.Context
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

/**
 * Áp font của app nhúng module lên các TextView của hộp thoại.
 *
 * Font không nằm trong layout mà set lúc chạy: module là thư viện dùng chung, hardcode
 * `@font/...` vào XML nghĩa là buộc mọi app nhúng nó phải có đúng tên font đó. App truyền font
 * của mình qua [RateConfig.fontRegular] / [RateConfig.fontBold]; không truyền thì giữ nguyên
 * font mặc định của hệ thống.
 */
internal fun TextView.applyRateFont(bold: Boolean) {
    val cfg = OneTabbRate.requireConfig()
    val fontRes = (if (bold) cfg.fontBold else cfg.fontRegular) ?: return
    runCatching { ResourcesCompat.getFont(context as Context, fontRes) }
        .getOrNull()
        ?.let { typeface = it }
}
