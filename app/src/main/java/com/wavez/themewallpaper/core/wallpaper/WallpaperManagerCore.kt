package com.wavez.themewallpaper.core.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.wavez.themewallpaper.core.wallpaper.cache.CacheManager
import com.wavez.themewallpaper.core.wallpaper.controller.LiveWallpaperController
import com.wavez.themewallpaper.core.wallpaper.controller.ParallaxController
import com.wavez.themewallpaper.core.wallpaper.controller.VideoWallpaperController
import com.wavez.themewallpaper.core.wallpaper.model.WallpaperOptions
import com.wavez.themewallpaper.core.wallpaper.model.WallpaperResult
import com.wavez.themewallpaper.core.wallpaper.model.WallpaperSource
import com.wavez.themewallpaper.core.wallpaper.processor.BitmapUtils
import com.wavez.themewallpaper.core.wallpaper.processor.WallpaperProcessor
import com.wavez.themewallpaper.core.wallpaper.processor.safeRecycle
import com.wavez.themewallpaper.core.wallpaper.setter.WallpaperSetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Single entry point for the WallpaperCore module.
 *
 * Instantiate once (singleton / Hilt @Singleton) and inject wherever needed.
 *
 * ─────────────────────────────────────────────────────────────
 *  Complete applyWallpaper() pipeline
 * ─────────────────────────────────────────────────────────────
 *
 *   URI / source
 *       │
 *       ▼
 *   CacheManager.getOrLoad()  ─── cache hit? ──► skip decode+process
 *       │ miss
 *       ▼
 *   BitmapUtils.decode()       memory-safe inSampleSize decode
 *       │
 *       ▼
 *   WallpaperProcessor.process()
 *       ├── scale     (CROP_CENTER / FIT_CENTER / STRETCH)
 *       ├── blur      (Box Blur, downscale trick, no RenderScript)
 *       ├── color     (brightness + contrast + saturation, single ColorMatrix pass)
 *       └── overlay   (dark overlay via PorterDuff)
 *       │
 *       ▼
 *   CacheManager.putDisk()     persist processed result
 *       │
 *       ▼
 *   WallpaperSetter.set()      FLAG_SYSTEM / FLAG_LOCK / both
 *       └── ManufacturerCompat shims (Samsung One UI, Xiaomi MIUI)
 *
 * ─────────────────────────────────────────────────────────────
 *  Usage
 * ─────────────────────────────────────────────────────────────
 *
 * ```kotlin
 * val core = WallpaperManagerCore(context)
 *
 * // Apply a static image
 * core.applyWallpaper(
 *     WallpaperOptions(
 *         source          = WallpaperSource.StaticImage(uri),
 *         target          = WallpaperTarget.BOTH,
 *         scaleType       = ScaleType.CROP_CENTER,
 *         blurRadius      = 12f,
 *         darkOverlayAlpha = 0.3f,
 *     )
 * ).collect { result ->
 *     when (result) {
 *         is WallpaperResult.Progress -> showProgress(result.percent, result.step)
 *         is WallpaperResult.Success  -> showSuccess()
 *         is WallpaperResult.Error    -> showError(result.message)
 *     }
 * }
 *
 * // Parallax preview
 * core.parallax.offsets(intensity = 0.3f)
 *     .onEach { (x, y) -> updatePreviewOffset(x, y) }
 *     .launchIn(viewModelScope)
 *
 * // Video wallpaper
 * core.video.apply(videoUri, MyVideoWallpaperService::class.java)
 *
 * // Live wallpaper
 * core.live.preview(ComponentName(pkg, cls))
 * ```
 */
class WallpaperManagerCore(context: Context) {

    private val ctx    = context.applicationContext
    private val setter = WallpaperSetter(ctx)
    private val cache  = CacheManager(ctx)

    val parallax = ParallaxController(ctx)
    val video    = VideoWallpaperController(ctx)
    val live     = LiveWallpaperController(ctx)

    // ── Main pipeline ────────────────────────────────────────────────────────

    /**
     * Fully processes and applies a wallpaper as described by [options].
     *
     * Emits [WallpaperResult.Progress] steps so the UI can display progress,
     * followed by exactly one terminal [WallpaperResult.Success] or
     * [WallpaperResult.Error].
     *
     * The flow is cold — it starts work only when collected.
     */
    fun applyWallpaper(options: WallpaperOptions): Flow<WallpaperResult<Unit>> = flow {
        if (!setter.isSupported) {
            emit(WallpaperResult.Error(UnsupportedOperationException(
                "Wallpaper is not supported on this device."
            )))
            return@flow
        }
        if (!setter.isAllowed) {
            emit(WallpaperResult.Error(SecurityException(
                "Setting wallpaper is not allowed on this device."
            )))
            return@flow
        }

        try {
            emit(WallpaperResult.Progress(5, "Preparing"))

            when (val source = options.source) {

                is WallpaperSource.StaticImage -> {
                    emit(WallpaperResult.Progress(15, "Decoding image"))
                    applyStatic(source.uri, options)
                    emit(WallpaperResult.Progress(95, "Applying"))
                }

                is WallpaperSource.Gif -> {
                    emit(WallpaperResult.Progress(15, "Decoding GIF frame"))
                    // GIF as static: extract first frame.
                    // For animated GIF wallpaper, use a LiveWallpaperService instead.
                    applyGifFirstFrame(source.uri, options)
                    emit(WallpaperResult.Progress(95, "Applying"))
                }

                is WallpaperSource.Video -> {
                    emit(WallpaperResult.Error(UnsupportedOperationException(
                        "Video wallpaper requires a VideoWallpaperService. " +
                        "Use core.video.apply(uri, MyVideoWallpaperService::class.java)."
                    )))
                    return@flow
                }

                is WallpaperSource.Live -> {
                    live.preview(source.componentName)
                    emit(WallpaperResult.Progress(100, "Done"))
                    emit(WallpaperResult.Success(Unit))
                    return@flow
                }
            }

            emit(WallpaperResult.Progress(100, "Done"))
            emit(WallpaperResult.Success(Unit))

        } catch (e: Exception) {
            emit(WallpaperResult.Error(e))
        }
    }

    // ── Preview (no wallpaper set) ───────────────────────────────────────────

    /**
     * Returns a processed Bitmap suitable for displaying as a preview inside the app.
     * Nothing is set as the system wallpaper. The caller owns the returned Bitmap
     * and must recycle it when done.
     *
     * @param targetWidth  Width of the preview area in pixels.
     * @param targetHeight Height of the preview area in pixels.
     */
    suspend fun preview(
        uri: Uri,
        options: WallpaperOptions,
        targetWidth: Int,
        targetHeight: Int,
    ): Bitmap = withContext(Dispatchers.Default) {
        val raw = BitmapUtils.decode(ctx, uri, targetWidth, targetHeight)
        WallpaperProcessor.process(raw, options, targetWidth, targetHeight)
            .also { if (it !== raw) raw.safeRecycle() }
    }

    // ── Cache management ─────────────────────────────────────────────────────

    fun clearMemoryCache() = cache.clearMemory()

    suspend fun clearAllCache() = cache.clearAll()

    /** Remove disk entries older than [maxDiskBytes] bytes (oldest-first). */
    suspend fun trimDiskCache(maxDiskBytes: Long = 50 * 1024 * 1024L) =
        cache.trimDisk(maxDiskBytes)

    suspend fun diskCacheSizeBytes(): Long = cache.diskSizeBytes()

    // ── Internal ─────────────────────────────────────────────────────────────

    private suspend fun applyStatic(uri: Uri, options: WallpaperOptions) {
        val (w, h) = BitmapUtils.screenSize(ctx)
        val key = cacheKey(uri, options)

        val processed = cache.getOrLoad(key) {
            val raw = BitmapUtils.decode(ctx, uri, w, h)
            WallpaperProcessor.process(raw, options, w, h)
                .also { if (it !== raw) raw.safeRecycle() }
        }

        setter.set(processed, options.target.flag)
    }

    private suspend fun applyGifFirstFrame(uri: Uri, options: WallpaperOptions) {
        val (w, h) = BitmapUtils.screenSize(ctx)
        val raw = BitmapUtils.decode(ctx, uri, w, h)
        val processed = WallpaperProcessor.process(raw, options, w, h)
            .also { if (it !== raw) raw.safeRecycle() }
        setter.set(processed, options.target.flag)
    }

    private fun cacheKey(uri: Uri, options: WallpaperOptions): String =
        "$uri|${options.scaleType}|${options.blurRadius}|${options.brightness}|" +
        "${options.contrast}|${options.saturation}|${options.darkOverlayAlpha}"
}
