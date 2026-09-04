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

package com.onetabb.template.android.data.local

import android.content.SharedPreferences

interface SharedPref {

    val sharedPref: SharedPreferences

    val editor: SharedPreferences.Editor

    fun getConfigLanguage(): String

    fun setConfigLanguage(language: String)

    val deviceId: String

    /** Còn phải qua màn Language (LFO) — lật false ở trang cuối onboarding. */
    var isNeedShowLanguage: Boolean

    /** Chưa xong onboarding. Đây là mốc "phiên đầu" cho Rule Engine của ads. */
    var isNeedGotoWalkthrough: Boolean

    /** Đã từng mở app (lật ngay cuối Splash lần đầu). */
    var hasOpenedBefore: Boolean
}
