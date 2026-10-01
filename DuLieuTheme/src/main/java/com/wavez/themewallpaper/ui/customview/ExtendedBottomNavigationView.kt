package com.wavez.themewallpaper.ui.customview

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.bottomnavigation.BottomNavigationView

open class ExtendedBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.bottomNavigationStyle,
    defStyleRes: Int = com.google.android.material.R.style.Widget_Design_BottomNavigationView
) : BottomNavigationView(context, attrs, defStyleAttr, defStyleRes) {

    override fun getMaxItemCount(): Int = 8
}
