package com.wavez.controllerwidget.ui.customview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.wavez.controllerwidget.R

class IconTextButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val iconView: AppCompatImageView
    private val textView: AppCompatTextView

    init {
        inflate(context, R.layout.layout_icon_button, this)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        iconView = findViewById(R.id.btnIcon)
        textView = findViewById(R.id.btnText)

        context.obtainStyledAttributes(attrs, R.styleable.IconTextButton).use {
            val iconRes = it.getResourceId(R.styleable.IconTextButton_btnIcon, 0)
            setIcon(iconRes)

            textView.text = it.getString(R.styleable.IconTextButton_btnText)

            val textColor = it.getColor(R.styleable.IconTextButton_btnTextColor, Color.WHITE)
            textView.setTextColor(textColor)

            it.getResourceId(R.styleable.IconTextButton_btnBackground, 0)
                .takeIf { res -> res != 0 }
                ?.let { res -> setBackgroundResource(res) }
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

    fun setButtonBackground(backgroundRes: Int) {
        setBackgroundResource(backgroundRes)
    }
}