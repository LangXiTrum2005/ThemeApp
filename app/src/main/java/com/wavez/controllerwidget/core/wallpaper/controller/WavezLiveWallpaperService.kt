package com.wavez.controllerwidget.core.wallpaper.controller

import android.net.Uri

class WavezLiveWallpaperService : LiveMediaWallpaperService() {
    override fun provideUri(): Uri? = LiveMediaController(this).getSavedUri()
}
