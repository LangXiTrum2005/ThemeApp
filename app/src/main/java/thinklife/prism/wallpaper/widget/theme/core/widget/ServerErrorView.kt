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
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.databinding.ViewServerErrorBinding
import kotlin.math.roundToInt

class ServerErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private val binding = ViewServerErrorBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val verticalPadding = (24 * resources.displayMetrics.density).roundToInt()
        setPadding(0, verticalPadding, 0, verticalPadding)

        context.withStyledAttributes(attrs, R.styleable.ServerErrorView) {
            getResourceId(R.styleable.ServerErrorView_errorIcon, R.drawable.ic_server_error)
                .let(::setIcon)
            getString(R.styleable.ServerErrorView_errorTitle)
                ?.let(::setTitle)
            getString(R.styleable.ServerErrorView_errorMessage)
                ?.let(::setMessage)
            getString(R.styleable.ServerErrorView_errorButtonText)
                ?.let(::setButtonText)
        }
    }

    fun setIcon(@DrawableRes iconRes: Int) {
        binding.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, iconRes))
    }

    fun setTitle(title: CharSequence) {
        binding.tvTitle.text = title
    }

    fun setTitle(@StringRes titleRes: Int) {
        binding.tvTitle.setText(titleRes)
    }

    fun setMessage(message: CharSequence) {
        binding.tvMessage.text = message
    }

    fun setMessage(@StringRes messageRes: Int) {
        binding.tvMessage.setText(messageRes)
    }

    fun setButtonText(text: CharSequence) {
        binding.btnRetry.text = text
    }

    fun setButtonText(@StringRes textRes: Int) {
        binding.btnRetry.setText(textRes)
    }

    fun setOnRetryClickListener(listener: OnClickListener?) {
        binding.btnRetry.setOnClickListener(listener)
    }
}
