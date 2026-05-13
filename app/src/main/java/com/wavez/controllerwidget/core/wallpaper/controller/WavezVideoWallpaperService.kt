package com.wavez.controllerwidget.core.wallpaper.controller

import android.net.Uri

/**
 * Concrete video wallpaper service.
 * Reads the video URI that was saved by [VideoWallpaperController.saveUri].
 *
 * Registered in AndroidManifest.xml — do not instantiate directly.
 */
class WavezVideoWallpaperService : VideoWallpaperService() {
    override fun provideVideoUri(): Uri? =
        VideoWallpaperController(this).getSavedUri()
}
