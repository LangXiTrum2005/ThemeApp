package com.wavez.controllerwidget.core.controllercenter.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import java.io.File
import java.nio.ByteBuffer

object UtilsKt {

    const val TAG = "Utils"

    fun openPreviewOrController(
        context: Context,
        isPreview: Boolean,
        onController: () -> Unit,
        onPreview: () -> Unit
    ) {

        if (isPreview) {
            onPreview.invoke()
        } else {
            onController.invoke()
        }
    }

    fun parseColorSafe(color: String): Int? {

        return try {
            color.toColorInt()
        } catch (_: Exception) {
            null
        }
    }

    fun loadImageOptimizeRam(
        imageView: ImageView,
        url: String? = null,
        uri: Uri? = null,
        file: File? = null,
        drawableRes: Int? = null,
        drawable: Drawable? = null,
        bitmap: Bitmap? = null,
        byteArray: ByteArray? = null,
        byteBuffer: ByteBuffer? = null
    ) {

        try {

            val glide = Glide.with(imageView.context)

            when {

                url != null -> {
                    glide
                        .load(url)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .into(imageView)
                }

                uri != null -> {
                    glide
                        .load(uri)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .into(imageView)
                }

                file != null -> {
                    glide
                        .load(file)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .into(imageView)
                }

                drawableRes != null -> {
                    glide
                        .load(drawableRes)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .into(imageView)
                }

                drawable != null -> {
                    glide
                        .load(drawable)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .into(imageView)
                }

                bitmap != null -> {
                    glide
                        .load(bitmap)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .into(imageView)
                }

                byteArray != null -> {
                    glide
                        .load(byteArray)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .into(imageView)
                }

                byteBuffer != null -> {
                    glide
                        .load(byteBuffer)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .into(imageView)
                }
            }

        } catch (_: Throwable) {
        }
    }

}