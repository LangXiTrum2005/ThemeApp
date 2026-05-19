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

    init {
        inflate(context, R.layout.layout_icon_button, this)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        context.obtainStyledAttributes(attrs, R.styleable.IconTextButton).use {
            val iconView = findViewById<AppCompatImageView>(R.id.btnIcon)
            val iconRes = it.getResourceId(
                R.styleable.IconTextButton_btnIcon,
                0
            )

            if (iconRes != 0) {
                iconView.visibility = VISIBLE
                iconView.setImageResource(iconRes)
            } else {
                iconView.visibility = GONE
            }

            val tv = findViewById<AppCompatTextView>(R.id.btnText)
            tv.text = it.getString(R.styleable.IconTextButton_btnText)

            val textColor = it.getColor(R.styleable.IconTextButton_btnTextColor, Color.WHITE)
            tv.setTextColor(textColor)
            it.getResourceId(R.styleable.IconTextButton_btnBackground, 0)
                .takeIf { res -> res != 0 }
                ?.let { res -> setBackgroundResource(res) }
        }
    }
}