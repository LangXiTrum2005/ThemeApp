package com.wavez.themewallpaper.ui.home.fragment.theme.transformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class ThemeBannerPageTransformer(
    private val pageTranslation: Float = DEFAULT_PAGE_TRANSLATION,
) : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val absolutePosition = abs(position).coerceAtMost(1f)
        val visibleFactor = 1f - absolutePosition
        page.scaleY = MIN_SCALE + visibleFactor * (MAX_SCALE - MIN_SCALE)
        page.alpha = MIN_ALPHA + visibleFactor * (MAX_ALPHA - MIN_ALPHA)
        page.translationX = -position * pageTranslation
    }

    private companion object {
        const val DEFAULT_PAGE_TRANSLATION = 28f
        const val MIN_SCALE = 0.92f
        const val MAX_SCALE = 1f
        const val MIN_ALPHA = 0.72f
        const val MAX_ALPHA = 1f
    }
}
