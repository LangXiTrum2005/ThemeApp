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

package thinklife.prism.wallpaper.widget.theme.core.wallpaper.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Two-level Bitmap cache.
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ L1  LruCache<String, Bitmap>  (in-process memory)               │
 * │     Size capped at [maxMemoryFraction] of the JVM heap.         │
 * │     Keys are MD5 hashes of the logical cache key.               │
 * ├──────────────────────────────────────────────────────────────────┤
 * │ L2  File-based disk cache     (app's cacheDir/wallpaper_cache/) │
 * │     Bitmaps stored as WEBP (≥ API 30) or WEBP legacy (API 29). │
 * │     No size limit enforced — callers should call [trimDisk]     │
 * │     periodically or rely on the OS clearing the cache dir.      │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Thread-safety:
 * - [LruCache] is internally synchronized.
 * - All disk I/O is confined to [Dispatchers.IO].
 *
 * OOM avoidance:
 * - Default fraction is 20 % of maxMemory, so the cache never consumes more
 *   than 1/5 of the heap on its own.
 * - [LruCache.sizeOf] returns [Bitmap.byteCount], so pressure is measured in
 *   bytes, not entry count.
 */
class CacheManager(
    context: Context,
    maxMemoryFraction: Float = 0.20f,
) {

    private val cacheDir = File(context.cacheDir, "wallpaper_cache").also { it.mkdirs() }

    private val memCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() * maxMemoryFraction.coerceIn(0.05f, 0.5f)).toInt()
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /** Checks L1 memory cache only. Returns null if not found. */
    fun getMemory(key: String): Bitmap? = memCache.get(key.hash())

    /** Checks L2 disk cache. Returns null if not found or decode fails. */
    suspend fun getDisk(key: String): Bitmap? = withContext(Dispatchers.IO) {
        val file = diskFile(key)
        if (!file.exists()) return@withContext null
        try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: Exception) {
            file.delete()   // Corrupt entry — remove it
            null
        }
    }

    /**
     * Returns a cached Bitmap from L1, then L2, then calls [loader] on a miss.
     * The result of [loader] is written to both caches before returning.
     */
    suspend fun getOrLoad(key: String, loader: suspend () -> Bitmap): Bitmap {
        val hash = key.hash()

        memCache.get(hash)?.let { return it }

        val fromDisk = getDisk(key)
        if (fromDisk != null) {
            memCache.put(hash, fromDisk)
            return fromDisk
        }

        val loaded = loader()
        memCache.put(hash, loaded)
        putDisk(key, loaded)
        return loaded
    }

    // ── Write ────────────────────────────────────────────────────────────────

    fun putMemory(key: String, bitmap: Bitmap) {
        memCache.put(key.hash(), bitmap)
    }

    suspend fun putDisk(key: String, bitmap: Bitmap): Unit = withContext(Dispatchers.IO) {
        try {
            FileOutputStream(diskFile(key)).use { out ->
                @Suppress("DEPRECATION")
                val format = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R)
                    Bitmap.CompressFormat.WEBP_LOSSY
                else
                    Bitmap.CompressFormat.WEBP
                bitmap.compress(format, 90, out)
            }
        } catch (_: Exception) { /* Non-fatal — disk might be full */ }
    }

    // ── Eviction ─────────────────────────────────────────────────────────────

    fun evictMemory(key: String) { memCache.remove(key.hash()) }

    fun clearMemory() { memCache.evictAll() }

    suspend fun clearDisk(): Unit = withContext(Dispatchers.IO) {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    suspend fun clearAll() {
        clearMemory()
        clearDisk()
    }

    /**
     * Removes disk entries until the total size is below [maxBytes].
     * Deletes oldest files first (by last-modified timestamp).
     */
    suspend fun trimDisk(maxBytes: Long): Unit = withContext(Dispatchers.IO) {
        val files = cacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return@withContext
        var total = files.sumOf { it.length() }
        for (file in files) {
            if (total <= maxBytes) break
            total -= file.length()
            file.delete()
        }
    }

    // ── Info ─────────────────────────────────────────────────────────────────

    fun memorySizeBytes(): Int = memCache.size()

    suspend fun diskSizeBytes(): Long = withContext(Dispatchers.IO) {
        cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun diskFile(key: String) = File(cacheDir, key.hash())

    private fun String.hash(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
