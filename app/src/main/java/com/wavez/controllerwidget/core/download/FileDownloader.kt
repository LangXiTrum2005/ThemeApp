package com.wavez.controllerwidget.core.download

import com.wavez.controllerwidget.core.di.DownloadClient
import com.wavez.controllerwidget.core.di.IoDispatcher
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
    @DownloadClient private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
        val request = Request.Builder().url(url).build()
        val response = try {
            okHttpClient.newCall(request).execute()
        } catch (e: IOException) {
            throw NetworkDownloadException("Network request failed: ${e.message}", e)
        }

        if (!response.isSuccessful) {
            response.close()
            throw NetworkDownloadException("Unexpected HTTP status: ${response.code}")
        }

        val body = response.body

        val contentLength = body.contentLength()
        destFile.parentFile?.mkdirs()

        try {
            body.byteStream().use { input ->
                destFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesRead += read
                        val percent = if (contentLength > 0) {
                            (bytesRead * 100L / contentLength).toInt().coerceIn(0, 99)
                        } else {
                            -1
                        }
                        onProgress(percent)
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
