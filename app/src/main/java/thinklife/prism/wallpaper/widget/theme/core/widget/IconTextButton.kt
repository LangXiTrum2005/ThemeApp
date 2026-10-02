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

package thinklife.prism.wallpaper.widget.theme.core.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import thinklife.prism.wallpaper.widget.theme.R

class IconTextButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val iconView: AppCompatImageView
    private val iconRightView: AppCompatImageView
    private val textView: AppCompatTextView

    init {
        inflate(context, R.layout.layout_icon_button, this)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        iconView = findViewById(R.id.btnIcon)
        iconRightView = findViewById(R.id.btnIconRight)
        textView = findViewById(R.id.btnText)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.IconTextButton)
        try {
            val iconRes = ta.getResourceId(R.styleable.IconTextButton_btnIcon, 0)
            setIcon(iconRes)

            val iconRightRes = ta.getResourceId(R.styleable.IconTextButton_btnIconRight, 0)
            setIconRight(iconRightRes)

            textView.text = ta.getString(R.styleable.IconTextButton_btnText)

            val textColor = ta.getColor(
                R.styleable.IconTextButton_btnTextColor,
                context.getColor(R.color.on_accent)
            )
            setTextColor(textColor)

            ta.getResourceId(R.styleable.IconTextButton_btnBackground, 0)
                .takeIf { it != 0 }
                ?.let { setBackgroundResource(it) }

        } finally {
            ta.recycle()
        }
    }

    fun setText(text: String) {
        textView.text = text
    }

    fun setTextColor(color: Int) {
        textView.setTextColor(color)
        // Icon cũ có màu trắng trong asset, nên phải tint cùng chữ trên nền Rose sáng.
        val tint = ColorStateList.valueOf(color)
        iconView.imageTintList = tint
        iconRightView.imageTintList = tint
    }

    fun setIcon(iconRes: Int) {
        if (iconRes != 0) {
            iconView.visibility = VISIBLE
            iconView.setImageResource(iconRes)
        } else {
            iconView.visibility = GONE
        }
    }

    fun setIconRight(iconRes: Int) {
        if (iconRes != 0) {
            iconRightView.visibility = VISIBLE
            iconRightView.setImageResource(iconRes)
        } else {
            iconRightView.visibility = GONE
        }
    }

    fun setButtonBackground(backgroundRes: Int) {
        setBackgroundResource(backgroundRes)
    }
}
