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

package thinklife.prism.wallpaper.widget.theme.core.wallpaper.processor

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Two-pass Box Blur with a downscale trick for large radii.
 *
 * No RenderScript / RenderEffect — runs on all API levels ≥ 29 at acceptable speed:
 * - radius ≤ 10  → blur in-place at full resolution
 * - radius ≤ 25  → downscale 2× before blur
 * - radius ≤ 50  → downscale 4×
 * - radius > 50  → downscale 8×
 *
 * Two horizontal + two vertical passes approach a Gaussian visually.
 *
 * Memory: one extra IntArray the size of the (downscaled) image.
 */
object BlurProcessor {

    suspend fun blur(bitmap: Bitmap, radius: Float): Bitmap {
        if (radius <= 0f) return bitmap
        return withContext(Dispatchers.Default) {
            applyBlur(bitmap, radius.toInt().coerceIn(1, 150))
        }
    }

    private fun applyBlur(source: Bitmap, radius: Int): Bitmap {
        val scale = when {
            radius > 50 -> 8f
            radius > 25 -> 4f
            radius > 10 -> 2f
            else -> 1f
        }
        val effectiveR = (radius / scale).toInt().coerceAtLeast(1)
        val w = (source.width / scale).toInt().coerceAtLeast(1)
        val h = (source.height / scale).toInt().coerceAtLeast(1)

        // Downscale
        val work = Bitmap.createScaledBitmap(source, w, h, true)
            .let { if (it.isMutable) it else it.copy(Bitmap.Config.ARGB_8888, true) }

        val src = IntArray(w * h)
        val tmp = IntArray(w * h)
        work.getPixels(src, 0, w, 0, 0, w, h)

        // Two passes for smoother result
        boxBlurH(src, tmp, w, h, effectiveR)
        boxBlurV(tmp, src, w, h, effectiveR)
        boxBlurH(src, tmp, w, h, effectiveR)
        boxBlurV(tmp, src, w, h, effectiveR)

        work.setPixels(src, 0, w, 0, 0, w, h)

        return if (scale > 1f) {
            // Upscale back to original size
            Bitmap.createScaledBitmap(work, source.width, source.height, true)
                .also { work.recycle() }
        } else {
            work
        }
    }

    /** Horizontal box blur pass: src → dst. */
    private fun boxBlurH(src: IntArray, dst: IntArray, w: Int, h: Int, r: Int) {
        val denom = 2 * r + 1
        for (y in 0 until h) {
            val row = y * w
            var rSum = 0; var gSum = 0; var bSum = 0
            // Seed the sliding window [-r .. r], clamping at boundaries
            for (i in -r..r) {
                val p = src[row + i.coerceIn(0, w - 1)]
                rSum += (p shr 16) and 0xff
                gSum += (p shr 8)  and 0xff
                bSum += p          and 0xff
            }
            for (x in 0 until w) {
                dst[row + x] = (src[row + x] and -0x1000000) or
                    ((rSum / denom) shl 16) or
                    ((gSum / denom) shl 8)  or
                    (bSum / denom)
                // Slide window right
                val remove = src[row + (x - r).coerceAtLeast(0)]
                val add    = src[row + (x + r + 1).coerceAtMost(w - 1)]
                rSum += ((add shr 16) and 0xff) - ((remove shr 16) and 0xff)
                gSum += ((add shr 8)  and 0xff) - ((remove shr 8)  and 0xff)
                bSum += (add          and 0xff) - (remove          and 0xff)
            }
        }
    }

    /** Vertical box blur pass: src → dst. */
    private fun boxBlurV(src: IntArray, dst: IntArray, w: Int, h: Int, r: Int) {
        val denom = 2 * r + 1
        for (x in 0 until w) {
            var rSum = 0; var gSum = 0; var bSum = 0
            for (i in -r..r) {
                val p = src[i.coerceIn(0, h - 1) * w + x]
                rSum += (p shr 16) and 0xff
                gSum += (p shr 8)  and 0xff
                bSum += p          and 0xff
            }
            for (y in 0 until h) {
                dst[y * w + x] = (src[y * w + x] and -0x1000000) or
                    ((rSum / denom) shl 16) or
                    ((gSum / denom) shl 8)  or
                    (bSum / denom)
                val remove = src[(y - r).coerceAtLeast(0)        * w + x]
                val add    = src[(y + r + 1).coerceAtMost(h - 1) * w + x]
                rSum += ((add shr 16) and 0xff) - ((remove shr 16) and 0xff)
                gSum += ((add shr 8)  and 0xff) - ((remove shr 8)  and 0xff)
                bSum += (add          and 0xff) - (remove          and 0xff)
            }
        }
    }
}
