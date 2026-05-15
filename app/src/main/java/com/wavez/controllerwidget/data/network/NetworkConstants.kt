package com.wavez.controllerwidget.data.network

object NetworkConstants {
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    // Longer timeouts for streaming file downloads
    const val DOWNLOAD_READ_TIMEOUT_SECONDS  = 120L
    const val DOWNLOAD_WRITE_TIMEOUT_SECONDS = 120L
}
