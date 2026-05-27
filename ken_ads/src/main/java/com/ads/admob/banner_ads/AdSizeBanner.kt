package com.ads.admob.banner_ads

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdSize

enum class AdSizeBanner {
    BANNER, LARGE_BANNER, MEDIUM_RECTANGLE, FULL_BANNER, LEADERBOARD, ADAPTIVE
}

fun AdSizeBanner.toAdSize(context: Context): AdSize {
    return when (this) {
        AdSizeBanner.BANNER -> AdSize.BANNER
        AdSizeBanner.LARGE_BANNER -> AdSize.LARGE_BANNER
        AdSizeBanner.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
        AdSizeBanner.FULL_BANNER -> AdSize.FULL_BANNER
        AdSizeBanner.LEADERBOARD -> AdSize.LEADERBOARD
        AdSizeBanner.ADAPTIVE -> getAdaptiveAdSize(context)
    }
}

fun getAdaptiveAdSize(context: Context): AdSize {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    val widthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // API 30+
        val windowMetrics = windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        val bounds = windowMetrics.bounds
        val usableWidth = bounds.width() - insets.left - insets.right
        usableWidth
    } else {
        // API < 30
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        outMetrics.widthPixels
    }

    val density = context.resources.displayMetrics.density
    val adWidth = (widthPixels / density).toInt()

    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
}