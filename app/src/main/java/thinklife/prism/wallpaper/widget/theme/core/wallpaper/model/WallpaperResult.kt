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

package thinklife.prism.wallpaper.widget.theme.core.wallpaper.model

sealed class WallpaperResult<out T> {

    data class Success<out T>(val data: T) : WallpaperResult<T>()

    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error",
    ) : WallpaperResult<Nothing>()

    data class Progress(
        val percent: Int,
        val step: String = "",
    ) : WallpaperResult<Nothing>()
}

inline val WallpaperResult<*>.isSuccess get() = this is WallpaperResult.Success
inline val WallpaperResult<*>.isError   get() = this is WallpaperResult.Error

inline fun <T> WallpaperResult<T>.onSuccess(block: (T) -> Unit): WallpaperResult<T> {
    if (this is WallpaperResult.Success) block(data)
    return this
}

inline fun <T> WallpaperResult<T>.onError(block: (Throwable, String) -> Unit): WallpaperResult<T> {
    if (this is WallpaperResult.Error) block(exception, message)
    return this
}
