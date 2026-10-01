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

fun Bitmap.safeRecycle() {
    if (!isRecycled) recycle()
}

fun Bitmap.ensureMutable(): Bitmap =
    if (isMutable) this else copy(Bitmap.Config.ARGB_8888, true)
