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

package thinklife.prism.wallpaper.widget.theme.core.extensions

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import thinklife.prism.wallpaper.widget.theme.R

fun ImageView.loadWithLoading(
    model: Any?,
    loadingView: View,
    clearBeforeLoad: Boolean = true,
) {
    val loadingGroup = loadingView.findViewById<View>(R.id.loadingGroup)
    val errorGroup = loadingView.findViewById<View>(R.id.errorGroup)

    fun showLoading() {
        loadingView.isVisible = true
        loadingGroup?.isVisible = true
        errorGroup?.isVisible = false
    }

    fun showError() {
        loadingView.isVisible = true
        loadingGroup?.isVisible = false
        errorGroup?.isVisible = true
    }

    fun load() {
        if (clearBeforeLoad) {
            setImageDrawable(null)
        }
        showLoading()

        Glide.with(this)
            .load(model)
            // Tránh hardware bitmap (Config.HARDWARE): là nguyên nhân native SIGABRT
            // trên RenderThread của libhwui khi upload bitmap lên GPU.
            .disallowHardwareConfig()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    showError()
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    loadingView.isVisible = false
                    return false
                }
            })
            .into(this)
    }

    errorGroup?.setOnClickListener { load() }
    load()
}

fun ImageView.loadWithLoading(
    model: Any?,
    loadingView: View?,
    errorView: View?,
    clearBeforeLoad: Boolean = true,
) {
    fun showLoading() {
        loadingView?.isVisible = true
        loadingView?.isVisible = true
        errorView?.isVisible = false
    }

    fun showError() {
        loadingView?.isVisible = true
        loadingView?.isVisible = false
        errorView?.isVisible = true
    }

    fun load() {
        if (clearBeforeLoad) {
            setImageDrawable(null)
        }
        showLoading()

        Glide.with(this)
            .load(model)
            // Tránh hardware bitmap (Config.HARDWARE): là nguyên nhân native SIGABRT
            // trên RenderThread của libhwui khi upload bitmap lên GPU.
            .disallowHardwareConfig()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    showError()
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    loadingView?.isVisible = false
                    return false
                }
            })
            .into(this)
    }

    errorView?.setOnClickListener { load() }
    load()
}

