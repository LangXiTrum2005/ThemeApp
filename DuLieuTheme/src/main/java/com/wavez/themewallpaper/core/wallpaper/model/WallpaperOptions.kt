package com.wavez.themewallpaper.core.wallpaper.model

/**
 * Describes how a wallpaper should be decoded, processed, and applied.
 *
 * @param source         Input source (static image, GIF, video, or live wallpaper).
 * @param target         Screen target: HOME, LOCK, or BOTH.
 * @param scaleType      How the image is scaled to fit the screen.
 * @param blurRadius     0 = no blur; 1–150 = blur strength. Values > 25 use downscale trick.
 * @param brightness     Additive brightness shift: -255..255, 0 = unchanged.
 * @param contrast       Contrast multiplier: 0..2, 1.0 = unchanged.
 * @param saturation     Saturation multiplier: 0..2, 1.0 = unchanged, 0 = grayscale.
 * @param darkOverlayAlpha  0..1 opacity of a black overlay drawn on top.
 * @param parallaxEnabled   Whether parallax offset data should be emitted via [ParallaxController].
 * @param parallaxIntensity Clamp value for parallax offsets: 0..1.
 */
data class WallpaperOptions(
    val source: WallpaperSource,
    val target: WallpaperTarget = WallpaperTarget.BOTH,
    val scaleType: ScaleType = ScaleType.CROP_CENTER,
    val blurRadius: Float = 0f,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val darkOverlayAlpha: Float = 0f,
    val parallaxEnabled: Boolean = false,
    val parallaxIntensity: Float = 0.3f,
)
