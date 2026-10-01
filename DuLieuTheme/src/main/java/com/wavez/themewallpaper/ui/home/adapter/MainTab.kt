package com.wavez.themewallpaper.ui.home.adapter

import com.wavez.themewallpaper.R

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