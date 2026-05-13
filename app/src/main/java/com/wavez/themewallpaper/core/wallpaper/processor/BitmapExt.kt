package com.wavez.themewallpaper.core.wallpaper.processor

import android.graphics.Bitmap

fun Bitmap.safeRecycle() {
    if (!isRecycled) recycle()
}

fun Bitmap.ensureMutable(): Bitmap =
    if (isMutable) this else copy(Bitmap.Config.ARGB_8888, true)
