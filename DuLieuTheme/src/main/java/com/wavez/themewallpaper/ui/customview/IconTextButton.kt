package com.wavez.themewallpaper.ui.customview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.wavez.themewallpaper.R

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
                Color.WHITE
            )
            textView.setTextColor(textColor)

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
