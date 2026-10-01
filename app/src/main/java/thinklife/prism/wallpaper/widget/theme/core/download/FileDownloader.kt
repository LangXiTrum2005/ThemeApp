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

package thinklife.prism.wallpaper.widget.theme.core.download

import thinklife.prism.wallpaper.widget.theme.di.DownloadClient
import thinklife.prism.wallpaper.widget.theme.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileDownloader @Inject constructor(
    @param:DownloadClient private val okHttpClient: OkHttpClient,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Downloads [url] and writes bytes to [destFile].
     * Reports progress (0–100, or -1 when content-length is unknown) via [onProgress].
     * Throws on network failure, write failure, or empty result.
     * Caller is responsible for providing a temp [destFile] and for cleanup on failure.
     */
    suspend fun download(
        url: String,
        destFile: File,
        onProgress: suspend (percent: Int) -> Unit,
    ): File = withContext(ioDispatcher) {
        val request = Request.Builder()
            .url(url)
            .header("Accept-Encoding", "identity")
            .build()
        val response = try {
            okHttpClient.newCall(request).execute()
        } catch (e: IOException) {
            throw NetworkDownloadException("Network request failed: ${e.message}", e)
        }

        if (!response.isSuccessful) {
            response.close()
            throw NetworkDownloadException("Unexpected HTTP status: ${response.code}")
        }

        val body = response.body ?: throw NetworkDownloadException("Empty response body")

        val contentLength = body.contentLength()
        destFile.parentFile?.mkdirs()
        onProgress(if (contentLength > 0) 0 else -1)

        try {
            body.byteStream().use { input ->
                destFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead = 0L
                    var lastPercent = if (contentLength > 0) 0 else -1
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesRead += read
                        val percent = if (contentLength > 0) {
                            (bytesRead * 100L / contentLength).toInt().coerceIn(0, 99)
                        } else {
                            -1
                        }
                        if (percent != lastPercent) {
                            lastPercent = percent
                            onProgress(percent)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            throw FileWriteDownloadException("Failed writing to ${destFile.absolutePath}: ${e.message}", e)
        }

        if (destFile.length() == 0L) {
            throw EmptyFileDownloadException("Downloaded file is empty: ${destFile.absolutePath}")
        }

        destFile
    }
}

class NetworkDownloadException(message: String, cause: Throwable? = null) : IOException(message, cause)
class FileWriteDownloadException(message: String, cause: Throwable? = null) : IOException(message, cause)
class EmptyFileDownloadException(message: String) : IOException(message)
