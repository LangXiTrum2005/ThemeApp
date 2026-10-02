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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BitmapUtils {

    /**
     * Memory-efficient bitmap decode.
     *
     * Two-pass strategy:
     *   1. Decode bounds only (zero heap allocation).
     *   2. Compute inSampleSize so the decoded size ≥ [targetWidth]×[targetHeight]
     *      while being as small as possible to avoid OOM.
     *
     * The result is always ARGB_8888 mutable so downstream processors can write in place.
     */
    suspend fun decode(
        context: Context,
        uri: Uri,
        targetWidth: Int,
        targetHeight: Int,
    ): Bitmap = withContext(Dispatchers.IO) {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }

        opts.inSampleSize = calculateInSampleSize(opts, targetWidth, targetHeight)
        opts.inJustDecodeBounds = false
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888
        opts.inMutable = true

        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: throw IllegalStateException("Cannot open stream for: $uri")
    }

    /**
     * Computes the largest power-of-two inSampleSize such that the down-sampled
     * image is still at least [reqW]×[reqH].
     */
    fun calculateInSampleSize(
        opts: BitmapFactory.Options,
        reqW: Int,
        reqH: Int,
    ): Int {
        val h = opts.outHeight
        val w = opts.outWidth
        var sample = 1
        if (h > reqH || w > reqW) {
            val halfH = h / 2
            val halfW = w / 2
            while (halfH / sample >= reqH && halfW / sample >= reqW) sample *= 2
        }
        return sample
    }

    /** Physical screen pixels (width to height). */
    fun screenSize(context: Context): Pair<Int, Int> {
        val m = context.resources.displayMetrics
        return m.widthPixels to m.heightPixels
    }
}
