package com.wavez.controllerwidget.ui.customview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.extensions.gone

class ThemeTabIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val ivIcon: AppCompatImageView
    private val tvName: TextView

    private var activeBackground: Drawable? = null
    private var inactiveBackground: Drawable? = null
    private var activeTextColor: Int = Color.BLACK
    private var inactiveTextColor: Int = Color.GRAY

    init {
        // Inflate the layout
        inflate(context, R.layout.item_tab_category, this)

        // Find views
        ivIcon = findViewById(R.id.ivIcon)
        tvName = findViewById(R.id.tvTitle)

        // Parse custom attributes
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThemeTabIcon)
        try {
            // Initialize backgrounds and text colors from XML attributes
            activeBackground = typedArray.getDrawable(R.styleable.ThemeTabIcon_tabActiveBackground)
            inactiveBackground =
                typedArray.getDrawable(R.styleable.ThemeTabIcon_tabInactiveBackground)

            activeTextColor = typedArray.getColor(
                R.styleable.ThemeTabIcon_tabActiveTextColor,
                context.getColor(R.color.white),
            )
            inactiveTextColor = typedArray.getColor(
                R.styleable.ThemeTabIcon_tabInactiveTextColor,
                context.getColor(R.color.med_em),
            )

            // Set initial state to inactive
            inactivate()

            // Set other attributes like icon and text
            val iconDrawable = typedArray.getDrawable(R.styleable.ThemeTabIcon_tabIcon)
            setIcon(iconDrawable)
            tvName.text = typedArray.getString(R.styleable.ThemeTabIcon_tabText)
        } finally {
            typedArray.recycle()
        }
    }

    /**
     * Function to activate the tab (update background and text color)
     */
    fun activate() {
        background = activeBackground
        ivIcon.isSelected = true
        tvName.setTextColor(activeTextColor)
    }

    /**
     * Function to inactivate the tab (update background and text color)
     */
    fun inactivate() {
        background = inactiveBackground
        ivIcon.isSelected = false
        tvName.setTextColor(inactiveTextColor)
    }

    /**
     * Set active background programmatically
     */
    fun setActiveBackground(drawable: Drawable?) {
        activeBackground = drawable
    }

    /**
     * Set inactive background programmatically
     */
    fun setInactiveBackground(drawable: Drawable?) {
        inactiveBackground = drawable
    }

    /**
     * Set active text color programmatically
     */
    fun setActiveTextColor(color: Int) {
        activeTextColor = color
    }

    /**
     * Set inactive text color programmatically
     */
    fun setInactiveTextColor(color: Int) {
        inactiveTextColor = color
    }

    /**
     * Function to set the icon drawable using Glide or directly
     */
    fun setIcon(icon: Drawable?) {
        if (icon != null) {
            ivIcon.isVisible = true
            ivIcon.setImageDrawable(icon)
        } else {
            ivIcon.gone()
        }
    }

    /**
     * Overloaded function to set the icon using a URL
     */
    fun setIcon(iconUrl: String?) {
        if (!iconUrl.isNullOrEmpty()) {
            ivIcon.isVisible = true
            Glide.with(context)
                .load(iconUrl)
                .into(ivIcon)
        } else {
            ivIcon.gone()
        }
    }

    fun setName(name: String) {
        tvName.text = name
    }
}
