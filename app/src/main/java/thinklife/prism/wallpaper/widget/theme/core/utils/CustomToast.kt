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

package thinklife.prism.wallpaper.widget.theme.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.extensions.gone

class CustomToast(context: Context) : Toast(context) {

    companion object {

        fun makeText(
            context: Context,
            @SuppressLint("SupportAnnotationUsage") @StringRes contentId: Int,
            @DrawableRes bgDrawable: Int? = null,
            @DrawableRes iconId: Int? = null,
            gravity: Int = Gravity.BOTTOM,
            duration: Int = LENGTH_SHORT,
            textColor: Int = R.color.black
        ): Toast {
            val toast = Toast(context)
            toast.duration = duration

            val layout: View =
                LayoutInflater.from(context).inflate(R.layout.layout_toast, null, false)

            val bgRoot = layout.findViewById<View>(R.id.bgRoot) as ConstraintLayout
            val icon = layout.findViewById<View>(R.id.imgIcon) as AppCompatImageView
            val text = layout.findViewById<View>(R.id.tvContent) as TextView

            bgDrawable?.let { bgRoot.setBackgroundResource(it) }
            iconId?.let {
                icon.setImageResource(it)
            } ?: run {
                icon.gone()
            }
            if (iconId != 0) iconId?.let { icon.setImageResource(it) }
            else {
                icon.gone()
            }
            text.text = context.getString(contentId)
            text.setTextColor(context.getColor(textColor))

            try {
                toast.view = layout
                toast.setGravity(gravity, 0, 0)
            } catch (_: Exception) {
                toast.setText(context.getString(contentId))
            }
            toast.duration = duration
            return toast
        }
    }

}