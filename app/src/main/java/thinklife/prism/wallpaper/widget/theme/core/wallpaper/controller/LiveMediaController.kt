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

package thinklife.prism.wallpaper.widget.theme.core.wallpaper.controller

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.edit

class LiveMediaController(context: Context) {

    private val ctx   = context.applicationContext
    private val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun apply(uri: Uri, serviceClass: Class<*>) {
        prefs.edit { putString(KEY_URI, uri.toString()) }
        ctx.startActivity(
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(ctx, serviceClass))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun getSavedUri(): Uri? = prefs.getString(KEY_URI, null)?.let(Uri::parse)

    companion object {
        private const val PREFS_NAME = "wavez_live_wallpaper"
        private const val KEY_URI    = "media_uri"
    }
}
