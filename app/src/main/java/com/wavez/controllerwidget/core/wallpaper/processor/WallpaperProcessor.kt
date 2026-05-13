package com.wavez.controllerwidget.core.wallpaper.processor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import com.wavez.controllerwidget.core.wallpaper.model.ScaleType
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap

/**
 * Pure bitmap transformation pipeline.
 * All functions are stateless. Every function that creates a new Bitmap documents
 * whether the [source] is recycled or returned as-is.
 */
object WallpaperProcessor {

    /**
     * Full pipeline: scale → blur → color-adjust → dark-overlay.
     * The caller is responsible for recycling [source] if the returned Bitmap differs.
     */
    suspend fun process(
        source: Bitmap,
        options: WallpaperOptions,
        targetWidth: Int,
        targetHeight: Int,
    ): Bitmap = withContext(Dispatchers.Default) {
        var current = scale(source, targetWidth, targetHeight, options.scaleType)

        if (options.blurRadius > 0f) {
            val blurred = BlurProcessor.blur(current, options.blurRadius)
            if (blurred !== current) current.safeRecycle()
            current = blurred
        }

        val needsColorAdjust = options.brightness != 0f ||
            options.contrast != 1f ||
            options.saturation != 1f
        if (needsColorAdjust) {
            val adjusted = adjustColors(current, options.brightness, options.contrast, options.saturation)
            if (adjusted !== current) current.safeRecycle()
            current = adjusted
        }

        if (options.darkOverlayAlpha > 0f) {
            current = applyDarkOverlay(current, options.darkOverlayAlpha)
        }

        current
    }

    // ──────────────────────────────────────────────────────────
    // Scale
    // ──────────────────────────────────────────────────────────

    /**
     * Returns a new [targetWidth]×[targetHeight] Bitmap.
     *
     * - CROP_CENTER: scale-to-fill (cover), centre, crop excess.
     * - FIT_CENTER : scale-to-fit (contain), centre, pad with black.
     * - STRETCH    : non-uniform scale to exact target size.
     */
    fun scale(source: Bitmap, targetW: Int, targetH: Int, scaleType: ScaleType): Bitmap {
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()
        val matrix = Matrix()

        when (scaleType) {
            ScaleType.STRETCH -> {
                matrix.setScale(targetW / srcW, targetH / srcH)
            }
            ScaleType.CROP_CENTER -> {
                val s = maxOf(targetW / srcW, targetH / srcH)
                matrix.setScale(s, s)
                matrix.postTranslate((targetW - srcW * s) / 2f, (targetH - srcH * s) / 2f)
            }
            ScaleType.FIT_CENTER -> {
                val s = minOf(targetW / srcW, targetH / srcH)
                matrix.setScale(s, s)
                matrix.postTranslate((targetW - srcW * s) / 2f, (targetH - srcH * s) / 2f)
            }
        }

        val result = createBitmap(targetW, targetH)
        Canvas(result).apply {
            if (scaleType == ScaleType.FIT_CENTER) drawColor(Color.BLACK)
            drawBitmap(source, matrix, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        }
        return result
    }

    // ──────────────────────────────────────────────────────────
    // Color adjustments (ColorMatrix, single-pass)
    // ──────────────────────────────────────────────────────────

    /**
     * Applies brightness + contrast + saturation in a single ColorMatrix pass.
     *
     * @param brightness Additive shift per channel: -255..255 (0 = no change).
     * @param contrast   Multiplicative factor: 0..2 (1.0 = no change).
     * @param saturation Saturation factor: 0..2 (1.0 = no change, 0 = grayscale).
     *
     * Returns a new Bitmap; [source] is not recycled.
     */
    fun adjustColors(
        source: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float,
    ): Bitmap {
        // Brightness + contrast: scale each channel around the midpoint (128)
        // translate = (1 - contrast) * 128 + brightness
        val translate = (1f - contrast) * 128f + brightness
        val bcMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f,
        ))

        // Saturation matrix, with brightness+contrast pre-concatenated
        val combined = ColorMatrix().apply {
            setSaturation(saturation)
            preConcat(bcMatrix)
        }

        val result = createBitmap(source.width, source.height)
        Canvas(result).drawBitmap(source, 0f, 0f, Paint().apply {
            colorFilter = ColorMatrixColorFilter(combined)
        })
        return result
    }

    // ──────────────────────────────────────────────────────────
    // Dark overlay
    // ──────────────────────────────────────────────────────────

    /**
     * Paints a semi-transparent black layer on top of [source].
     * Modifies [source] in place if it is already mutable; otherwise creates a copy.
     *
     * @param alpha 0..1 (0 = transparent, 1 = fully black).
     */
    fun applyDarkOverlay(source: Bitmap, alpha: Float): Bitmap {
        val result = source.ensureMutable()
        Canvas(result).drawColor(
            Color.argb((alpha.coerceIn(0f, 1f) * 255).toInt(), 0, 0, 0),
            PorterDuff.Mode.SRC_OVER,
        )
        return result
    }
}
