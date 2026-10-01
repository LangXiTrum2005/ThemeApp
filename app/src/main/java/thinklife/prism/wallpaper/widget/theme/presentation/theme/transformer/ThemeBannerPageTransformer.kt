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

package thinklife.prism.wallpaper.widget.theme.presentation.theme.transformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class ThemeBannerPageTransformer(
    private val pageTranslation: Float = DEFAULT_PAGE_TRANSLATION,
) : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val absolutePosition = abs(position).coerceAtMost(1f)
        val visibleFactor = 1f - absolutePosition
        val scale = MIN_SCALE + visibleFactor * (MAX_SCALE - MIN_SCALE)
        
        page.scaleX = scale
        page.scaleY = scale
        page.alpha = MIN_ALPHA + visibleFactor * (MAX_ALPHA - MIN_ALPHA)
        page.translationX = -position * pageTranslation
        page.translationZ = visibleFactor * 12f
        
        // Subtle 3D rotation for a floating carousel feel
        page.cameraDistance = 8000f * page.resources.displayMetrics.density
        page.rotationY = -position * 7f
    }

    private companion object {
        const val DEFAULT_PAGE_TRANSLATION = 32f
        const val MIN_SCALE = 0.88f
        const val MAX_SCALE = 1f
        const val MIN_ALPHA = 0.75f
        const val MAX_ALPHA = 1f
    }
}
