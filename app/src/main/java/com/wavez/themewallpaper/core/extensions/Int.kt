package com.wavez.themewallpaper.core.extensions

import kotlin.math.roundToInt
fun Int.getDurationDisplayFromMillis(): String {
    val ss = (this / 1000) % 60
    val mm = ((this / (1000 * 60)) % 60)
    val hours = ((this / (1000 * 60 * 60)) % 24)
    return if (hours != 0) {
        String.format("%02d:%02d:%02d", hours, mm, ss)
    } else {
        String.format("%02d:%02d", mm, ss)
    }
}

fun Int.getDurationDisplay2FromMillis(): String {
    val mS = this / 100
    val rear = mS % 10
    val second = mS / 10
    val realSecond = second % 60
    val minute = second / 60
    return if (minute < 10) {
        String.format("%02d:%02d.%d", minute, realSecond, rear)
    } else {
        String.format("%d:%02d.%d", minute, realSecond, rear)
    }
}

fun Int.getDurationDisplayFromOneOverTenSecond(): String {
    val rear = this % 10
    val second = this / 10
    val realSecond = second % 60
    val minute = second / 60
    return if (minute < 10) {
        String.format("%02d:%02d.%d", minute, realSecond, rear)
    } else {
        String.format("%d:%02d.%d", minute, realSecond, rear)
    }
}

fun Int.percentReverseToValue(totalValue: Int): Int = ((totalValue - ((totalValue).toFloat() / (100).toFloat() * this)).roundToInt())

fun Int.reverseValueToPercent(totalValue: Int): Int = (((totalValue - this) * 100) / totalValue).toFloat().toInt()

fun Int.percentToValue(totalValue: Int): Int = ((((totalValue).toFloat() / (100).toFloat() * this)).roundToInt())

fun Int.valueToPercent(totalValue: Int): Int = ((this * 100) / totalValue).toFloat().toInt()

fun Int.getDurationDisplayFromMillisToMinute(): String {
    val ss = (this / 1000) % 60
    val mm = ((this / (1000 * 60)) % 60)
    val hours = ((this / (1000 * 60 * 60)) % 24)
    return if (hours != 0) {
        String.format("%02d hour %02d min %02d secs", hours, mm, ss)
    } else {
        String.format("%02d min %02d secs", mm, ss)
    }
}

fun Int.toPercent(max: Int): Int {
    return if (max > 0) {
        ((this.toFloat() / max) * 100).toInt()
    } else 0
}