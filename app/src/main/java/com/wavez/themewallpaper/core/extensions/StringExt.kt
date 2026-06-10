package com.wavez.themewallpaper.core.extensions

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
    } else 0
    return "$percent%"
}