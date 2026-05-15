package com.wavez.controllerwidget.core.download

sealed class DownloadState<out T> {
    data object Idle : DownloadState<Nothing>()

    // percent in 0..100, or -1 when content-length is unknown
    data class Downloading(val percent: Int) : DownloadState<Nothing>()

    // controller theme only: zip is saved, extraction in progress
    data object Extracting : DownloadState<Nothing>()

    // file is on disk, Room record is about to be written
    data object Saving : DownloadState<Nothing>()

    data class Success<out T>(val file: T) : DownloadState<T>()
    data class Failure(val error: DownloadError) : DownloadState<Nothing>()
}
