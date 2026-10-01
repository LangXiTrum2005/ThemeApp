package com.wavez.themewallpaper.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.withStyledAttributes
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.databinding.ViewServerLoadingBinding
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
