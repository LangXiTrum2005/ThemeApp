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

package thinklife.prism.wallpaper.widget.theme.presentation.main.adapter

import thinklife.prism.wallpaper.widget.theme.R

enum class MainTab(
    val position: Int,
    val menuId: Int,
) {
    THEMES(0, R.id.nav_theme),
    WALLPAPERS(1, R.id.nav_wallpaper),
    WIDGETS(2, R.id.nav_widgets),
    CONTROLLER(3, R.id.nav_controller),
    ICONS(4, R.id.nav_icons),
    MY_GALLERY(5, R.id.nav_gallery);

    companion object {
        fun fromPosition(position: Int): MainTab {
            return entries.firstOrNull { it.position == position } ?: THEMES
        }

        fun fromMenuId(menuId: Int): MainTab {
            return entries.firstOrNull { it.menuId == menuId } ?: THEMES
        }
    }
}