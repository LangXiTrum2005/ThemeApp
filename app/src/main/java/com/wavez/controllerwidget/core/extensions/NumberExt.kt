package com.wavez.controllerwidget.core.extensions

import java.text.DateFormat
import java.util.Locale

fun convertTimeToHourMinuteSecond(time: Long): String {
    val secondTime = time / 1000
    val seconds = secondTime % 60
    val minutes = ((secondTime - seconds) / 60) % 60
    val hours = (secondTime - seconds - minutes * 60) / (60 * 60)
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
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
