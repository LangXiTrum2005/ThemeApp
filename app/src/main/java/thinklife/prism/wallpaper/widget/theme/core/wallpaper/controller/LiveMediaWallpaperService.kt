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

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.util.concurrent.Executors

abstract class LiveMediaWallpaperService : WallpaperService() {

    abstract fun provideUri(): Uri?

    final override fun onCreateEngine(): Engine = LiveMediaEngine()

    inner class LiveMediaEngine : Engine() {

        private val mainHandler  = Handler(Looper.getMainLooper())
        private val decodeExec   = Executors.newSingleThreadExecutor()
        private var player       : MediaPlayer?           = null
        private var gifDrawable  : AnimatedImageDrawable? = null

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            setTouchEventsEnabled(false)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            val uri = provideUri() ?: return
            val frame = holder.surfaceFrame
            if (uri.isGif()) loadGif(holder, uri, frame.width(), frame.height())
            else             loadVideo(holder, uri)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            gifDrawable?.setBounds(0, 0, width, height)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) { player?.start(); gifDrawable?.start() }
            else         { player?.pause(); gifDrawable?.stop()  }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            releaseAll()
        }

        override fun onDestroy() {
            super.onDestroy()
            releaseAll()
            decodeExec.shutdown()
        }

        // ── Renderers ─────────────────────────────────────────────────────────

        private fun loadVideo(holder: SurfaceHolder, uri: Uri) {
            val mp = MediaPlayer()
            try {
                mp.setDataSource(applicationContext, uri)
                mp.setSurface(holder.surface)
                mp.isLooping = true
                mp.setVolume(0f, 0f)
                mp.setOnPreparedListener { if (isVisible) it.start() }
                mp.setOnErrorListener { _, _, _ -> releasePlayer(); false }
                mp.prepareAsync()
                player = mp
            } catch (_: Exception) {
                mp.release()
            }
        }

        private fun loadGif(holder: SurfaceHolder, uri: Uri, w: Int, h: Int) {
            // Decode on background thread — ImageDecoder is blocking IO
            decodeExec.execute {
                try {
                    val d = ImageDecoder.decodeDrawable(
                        ImageDecoder.createSource(contentResolver, uri)
                    ) as? AnimatedImageDrawable ?: return@execute

                    mainHandler.post {
                        d.setBounds(0, 0, w, h)
                        d.callback = object : Drawable.Callback {
                            override fun invalidateDrawable(who: Drawable) {
                                val canvas = holder.lockCanvas() ?: return
                                try { who.draw(canvas) } finally { holder.unlockCanvasAndPost(canvas) }
                            }
                            override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
                                mainHandler.postAtTime(what, who, `when`)
                            }
                            override fun unscheduleDrawable(who: Drawable, what: Runnable) {
                                mainHandler.removeCallbacks(what, who)
                            }
                        }
                        gifDrawable = d
                        if (isVisible) d.start()
                    }
                } catch (_: Exception) { /* surface stays blank */ }
            }
        }

        // ── Helpers ───────────────────────────────────────────────────────────

        private fun releaseAll() {
            releasePlayer()
            gifDrawable?.stop()
            gifDrawable?.callback = null
            gifDrawable = null
        }

        private fun releasePlayer() {
            player?.run { if (isPlaying) stop(); setSurface(null); release() }
            player = null
        }

        private fun Uri.isGif(): Boolean {
            if (contentResolver.getType(this)?.contains("gif") == true) return true
            if (toString().substringBefore('?').endsWith(".gif", ignoreCase = true)) return true
            return try {
                contentResolver.openInputStream(this)?.use { stream ->
                    val h = ByteArray(3)
                    stream.read(h) == 3 &&
                    h[0] == 'G'.code.toByte() &&
                    h[1] == 'I'.code.toByte() &&
                    h[2] == 'F'.code.toByte()
                } == true
            } catch (_: Exception) { false }
        }
    }
}
