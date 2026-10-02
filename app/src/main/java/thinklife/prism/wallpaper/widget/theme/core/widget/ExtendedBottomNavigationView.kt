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
import com.google.android.material.bottomnavigation.BottomNavigationView

open class ExtendedBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.bottomNavigationStyle,
    defStyleRes: Int = com.google.android.material.R.style.Widget_Design_BottomNavigationView
) : BottomNavigationView(context, attrs, defStyleAttr, defStyleRes) {

    override fun getMaxItemCount(): Int = 8
}
