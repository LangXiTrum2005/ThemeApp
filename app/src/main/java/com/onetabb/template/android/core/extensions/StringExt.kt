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

import android.telephony.PhoneNumberUtils
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

fun String.normalized(): String = lowercase().filter { it.isLetterOrDigit() }

fun String.normalText(): String {
    val nfdNormalizedString: String = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern: Pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(nfdNormalizedString).replaceAll("").uppercase(Locale.getDefault())
        .trim()
}

fun getSpanName(name: String, strSearch: String, color: Int): SpannableString {
    val spanName = SpannableString(name)
    val fileNameSub = name.normalText().lowercase()
    var str = strSearch.normalText().lowercase()
    if (str != "") {
        try {
            val pattern = str.toRegex()
            val found = pattern.findAll(fileNameSub)
            found.forEach {
                spanName.setSpan(
                    ForegroundColorSpan(color),
                    it.range.first,
                    it.range.last + 1,
                    0,
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    return spanName
}

fun String.startsWithAnyIgnoreCase(prefixes: List<String>): Boolean {
    prefixes.forEach { prefix ->
        if (startsWith(prefix, true)) {
            return true
        }
    }
    return false
}

fun convertPhoneNumber(phoneNumber: String): String = if (phoneNumber.isEmpty()) {
    ""
} else {
    try {
        PhoneNumberUtils.formatNumber(
            phoneNumber,
            Locale.getDefault().country,
        )
    } catch (ex: Exception) {
        ""
    }
}

fun Int.toPercentText(max: Int): String {
    val percent = if (max > 0) {
        ((this.toFloat() / max) * 100).toInt()
    } else {
        0
    }
    return "$percent%"
}
