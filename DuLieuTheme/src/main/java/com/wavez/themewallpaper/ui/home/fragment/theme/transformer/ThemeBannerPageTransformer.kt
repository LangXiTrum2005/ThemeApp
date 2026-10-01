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
