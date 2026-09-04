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

fun <T> List<T>.findIndex(find: (T) -> Boolean): Int {
    for ((index, item) in withIndex()) {
        if (find(item)) {
            return index
        }
    }
    return -1
}

fun <T> Sequence<T>.findIndex(find: (T) -> Boolean): Int {
    for ((index, item) in withIndex()) {
        if (find(item)) {
            return index
        }
    }
    return -1
}

inline fun <T> List<T>.findItemAndIndex(condition: (T) -> Boolean, onFound: (Int, T) -> Unit) {
    for ((index, item) in withIndex()) {
        if (condition(item)) {
            onFound(index, item)
            return
        }
    }
}

fun <T> List<T>.containBy(condition: (T) -> Boolean): Boolean {
    for (item in this) {
        if (condition(item)) {
            return true
        }
    }
    return false
}

fun <T> MutableList<T>.removeBy(condition: (T) -> Boolean): Boolean {
    val value = find { condition(it) } ?: return false
    return remove(value)
}

fun <T> List<T>.equal(another: List<T>, onEqual: (T, T) -> Boolean): Boolean {
    if (this.size != another.size) return false
    for (item in this) {
        if (!another.containBy { onEqual(it, item) }) {
            return false
        }
    }
    return true
}

fun <T> MutableList<T>.insertAds(
    startIndex: Int,
    repeatInterval: Int,
    maxAds: Int,
    itemBuilder: () -> T,
) {
    if (maxAds == 0) return
    var startIndex = startIndex
    if (size > startIndex) {
        add(startIndex, itemBuilder())
    }
    startIndex += repeatInterval
    var adsCount = 0
    while (++adsCount < maxAds && startIndex < size) {
        add(startIndex, itemBuilder())
        startIndex += repeatInterval
    }
}
