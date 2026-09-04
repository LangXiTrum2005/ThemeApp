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

package com.onetabb.template.android.presentation.language.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class LanguageItem(
    /** Thẻ BCP-47 đầy đủ (`en-US`, `pt-BR`, `vi`) — truyền thẳng vào `AppCompatDelegate`. */
    val code: String,
    @StringRes val nameRes: Int,
    /** Cờ QUỐC GIA/VÙNG của ngôn ngữ. Nhiều ngôn ngữ cùng nước thì dùng chung 1 cờ (vd Ấn Độ). */
    @DrawableRes val flagRes: Int,
) {
    fun hasSameCode(other: LanguageItem): Boolean = code.equals(other.code, ignoreCase = true)

    fun matches(tag: String): Boolean = code.equals(tag, ignoreCase = true)
}
