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
import androidx.annotation.StringRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.withStyledAttributes
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.databinding.ViewServerLoadingBinding
import kotlin.math.roundToInt

class ServerLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private val binding = ViewServerLoadingBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val verticalPadding = (24 * resources.displayMetrics.density).roundToInt()
        setPadding(0, verticalPadding, 0, verticalPadding)

        context.withStyledAttributes(attrs, R.styleable.ServerLoadingView) {
            getString(R.styleable.ServerLoadingView_loadingMessage)
                ?.let(::setMessage)
        }
    }

    fun setMessage(message: CharSequence) {
        binding.tvMessage.text = message
    }

    fun setMessage(@StringRes messageRes: Int) {
        binding.tvMessage.setText(messageRes)
    }
}
