package com.wavez.controllerwidget.core.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.Glide
import com.wavez.controllerwidget.core.wallpaper.cache.CacheManager
import com.wavez.controllerwidget.core.wallpaper.controller.LiveMediaController
import com.wavez.controllerwidget.core.wallpaper.controller.LiveWallpaperController
import com.wavez.controllerwidget.core.wallpaper.controller.ParallaxController
import com.wavez.controllerwidget.core.wallpaper.controller.WavezLiveWallpaperService
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperOptions
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperResult
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperSource
import com.wavez.controllerwidget.core.wallpaper.processor.BitmapUtils
import com.wavez.controllerwidget.core.wallpaper.processor.WallpaperProcessor
import com.wavez.controllerwidget.core.wallpaper.processor.safeRecycle
import com.wavez.controllerwidget.core.wallpaper.setter.WallpaperSetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class WallpaperManagerCore(context: Context) {

    private val ctx   = context.applicationContext
    private val setter = WallpaperSetter(ctx)
    private val cache  = CacheManager(ctx)

    val parallax  = ParallaxController(ctx)
    val liveMedia = LiveMediaController(ctx)
    val live      = LiveWallpaperController(ctx)

    fun applyWallpaper(options: WallpaperOptions): Flow<WallpaperResult<Unit>> = flow {
        if (!setter.isSupported) {
            emit(WallpaperResult.Error(UnsupportedOperationException("Wallpaper not supported on this device.")))
            return@flow
        }
        if (!setter.isAllowed) {
            emit(WallpaperResult.Error(SecurityException("Setting wallpaper is not allowed on this device.")))
            return@flow
        }

        try {
            emit(WallpaperResult.Progress(5, "Preparing"))

            when (val src = options.source) {
                is WallpaperSource.StaticImage -> {
                    emit(WallpaperResult.Progress(15, "Decoding image"))
                    applyStatic(src.uri, options)
                    emit(WallpaperResult.Progress(95, "Applying"))
                }
                is WallpaperSource.OnlineImage -> {
                    emit(WallpaperResult.Progress(15, "Loading image"))
                    applyOnline(src.url, options)
                    emit(WallpaperResult.Progress(95, "Applying"))
                }
                is WallpaperSource.Gif   -> liveMedia.apply(src.uri, WavezLiveWallpaperService::class.java)
                is WallpaperSource.Video -> liveMedia.apply(src.uri, WavezLiveWallpaperService::class.java)
                is WallpaperSource.Live  -> live.preview(src.componentName)
            }

            emit(WallpaperResult.Progress(100, "Done"))
            emit(WallpaperResult.Success(Unit))

        } catch (e: Exception) {
            emit(WallpaperResult.Error(e))
        }
    }

    suspend fun preview(uri: Uri, options: WallpaperOptions, targetWidth: Int, targetHeight: Int): Bitmap =
        withContext(Dispatchers.Default) {
            val raw = BitmapUtils.decode(ctx, uri, targetWidth, targetHeight)
            WallpaperProcessor.process(raw, options, targetWidth, targetHeight)
                .also { if (it !== raw) raw.safeRecycle() }
        }

    fun clearMemoryCache() = cache.clearMemory()
    suspend fun clearAllCache() = cache.clearAll()
    suspend fun trimDiskCache(maxDiskBytes: Long = 50 * 1024 * 1024L) = cache.trimDisk(maxDiskBytes)
    suspend fun diskCacheSizeBytes(): Long = cache.diskSizeBytes()

    private suspend fun applyStatic(uri: Uri, options: WallpaperOptions) {
        val (w, h) = BitmapUtils.screenSize(ctx)
        val processed = cache.getOrLoad(cacheKey(uri, options)) {
            BitmapUtils.decode(ctx, uri, w, h).let { raw ->
                WallpaperProcessor.process(raw, options, w, h).also { if (it !== raw) raw.safeRecycle() }
            }
        }
        setter.set(processed, options.target.flag)
    }

    private suspend fun applyOnline(url: String, options: WallpaperOptions) {
        val (w, h) = BitmapUtils.screenSize(ctx)
        val raw = withContext(Dispatchers.IO) { Glide.with(ctx).asBitmap().load(url).submit(w, h).get() }
        val processed = WallpaperProcessor.process(raw, options, w, h)
            .also { if (it !== raw) raw.safeRecycle() }
        setter.set(processed, options.target.flag)
    }

    private fun cacheKey(uri: Uri, options: WallpaperOptions) =
        "$uri|${options.scaleType}|${options.blurRadius}|${options.brightness}|" +
        "${options.contrast}|${options.saturation}|${options.darkOverlayAlpha}"
}
