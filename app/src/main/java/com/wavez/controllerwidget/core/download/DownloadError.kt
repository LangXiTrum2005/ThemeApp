package com.wavez.controllerwidget.core.download

sealed class DownloadError(val cause: Throwable? = null) {
    data class NetworkError(val exception: Throwable) : DownloadError(exception)
    data class FileWriteError(val exception: Throwable) : DownloadError(exception)
    data object EmptyFile : DownloadError()
    data object InvalidZip : DownloadError()
    data class ExtractError(val exception: Throwable) : DownloadError(exception)
    data class MoveError(val exception: Throwable) : DownloadError(exception)
    data class UnknownError(val exception: Throwable) : DownloadError(exception)
}
