package com.wavez.controllerwidget.core.wallpaper.model

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
