package com.wavez.themewallpaper.core.controllercenter

import android.content.Context
import android.graphics.BitmapFactory
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Persistent files for the URL-based themes in control_data.json. */
object ControllerThemeAssets {
    private fun fingerprint(urls: List<String>) = MessageDigest.getInstance("SHA-256")
        .digest(urls.joinToString("\n").toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun themeDir(context: Context, themeId: String) =
        File(context.filesDir, "controller_themes/${themeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")}")

    private fun assetFile(context: Context, themeId: String, url: String): File {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(url.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return File(themeDir(context, themeId), hash)
    }

    fun resolve(context: Context, themeId: String, url: String): String {
        if (!url.startsWith("http")) return url
        val file = assetFile(context, themeId, url)
        return if (file.isFile && file.length() > 0L) file.absolutePath else url
    }

    fun isDownloaded(context: Context, themeId: String, urls: List<String>): Boolean =
        urls.isNotEmpty() &&
            File(themeDir(context, themeId), "complete").let {
                it.isFile && it.readText() == fingerprint(urls)
            } &&
            assetFile(context, themeId, urls.first()).let { it.isFile && it.length() > 0L }

    fun download(
        context: Context,
        themeId: String,
        urls: List<String>,
        requiredUrl: String,
        onProgress: (Int) -> Unit,
    ) {
        require(urls.isNotEmpty() && requiredUrl in urls)
        val dir = themeDir(context, themeId).apply { mkdirs() }
        val complete = File(dir, "complete")
        complete.delete()
        urls.forEachIndexed { index, url ->
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
            val target = assetFile(context, themeId, url)
            if (!target.isFile || target.length() == 0L) {
                val temporary = File(dir, "${target.name}.part")
                try {
                    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15000
                        readTimeout = 20000
                    }
                    try {
                        if (connection.responseCode !in 200..299) {
                            throw IllegalStateException("HTTP ${connection.responseCode}")
                        }
                        connection.inputStream.use { input ->
                            temporary.outputStream().use { output -> input.copyTo(output) }
                        }
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(temporary.absolutePath, options)
                        check(options.outWidth > 0 && options.outHeight > 0) { "Invalid image" }
                        check(temporary.renameTo(target)) { "Cannot save theme asset" }
                    } finally {
                        connection.disconnect()
                    }
                } catch (error: Exception) {
                    temporary.delete()
                    if (url == requiredUrl) throw error
                }
            }
            onProgress((index + 1) * 100 / urls.size)
        }
        check(assetFile(context, themeId, requiredUrl).isFile) { "Missing theme background" }
        complete.writeText(fingerprint(urls))
    }
}
