package com.wavez.controllerwidget.core.extensions

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Long.toFormattedDateString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = Date(this)
    return dateFormat.format(date)
}

fun Long.toFormattedDateString1(): String {
    val dateFormat = SimpleDateFormat("h:mm a • dd/MM/yyyy", Locale.getDefault())
    val date = Date(this)
    return dateFormat.format(date)
}


@SuppressLint("DefaultLocale")
fun Long.toFileSizeString(): String {
    val kb = this / 1024
    val mb = kb / 1024

    return when {
        mb > 0 -> String.format("%.0f Mb", mb.toFloat())
        kb > 0 -> String.format("%.0f Kb", kb.toFloat())
        else -> String.format("%d bytes", this)
    }
}

fun Long.getResolutionString(): String {
    val resolution = if (this > 1024 * 1024) {
        Pair(1200, 672)
    } else {
        Pair(800, 450)
    }
    return "${resolution.first} x ${resolution.second}"
}

@SuppressLint("DefaultLocale")
fun Long.getDurationMinSec(): String {
    val min = (this / (1000 * 60))
    val sec = (this / 1000) % 60
    return String.format("%02d:%02d", min, sec)
}


@SuppressLint("DefaultLocale")
fun Long.toDuration(): String {
    return try {
        val durationMs = this
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        String.format("%02d:%02d", minutes, seconds)
    } catch (e: Exception) {
        "00:00"
    }
}

fun String.toMiddleEllipsis(keepLastChars: Int): String {
    val ellipsis = "..."
    return if (this.length > (10 + keepLastChars)) {
        val keep = (10 - ellipsis.length - keepLastChars) / 2
        val startPart = this.take(keep)
        val endPart = this.takeLast(keepLastChars)
        "$startPart$ellipsis$endPart"
    } else {
        this
    }
}