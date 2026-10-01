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

package thinklife.prism.wallpaper.widget.theme.core.extensions

import android.view.View
import androidx.core.view.isVisible

fun switchVisibleWithSlideFade(
    showView: View,
    hideView: View,
    slideFromRight: Boolean,
    durationMs: Long = 180L,
    distancePx: Float = 24f,
) {
    if (showView.isVisible && !hideView.isVisible) return

    val distance = if (slideFromRight) -distancePx else distancePx
    hideView.animate()
        .alpha(0f)
        .translationX(distance)
        .setDuration(durationMs)
        .withEndAction {
            hideView.isVisible = false
            hideView.translationX = 0f
        }
        .start()

    showView.apply {
        isVisible = true
        alpha = 0f
        translationX = -distance
        animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(durationMs)
            .start()
    }
}

fun View.animateSegmentSelected(
    selected: Boolean,
    durationMs: Long = 140L,
) {
    animate()
        .alpha(if (selected) 1f else 0.72f)
        .scaleX(if (selected) 1f else 0.98f)
        .scaleY(if (selected) 1f else 0.98f)
        .setDuration(durationMs)
        .start()
}
