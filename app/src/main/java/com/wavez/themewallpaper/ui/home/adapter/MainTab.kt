package com.wavez.themewallpaper.ui.home.adapter

import com.wavez.themewallpaper.R

enum class MainTab(
    val position: Int,
    val menuId: Int,
) {
    THEMES(0, R.id.nav_theme),
    WALLPAPERS(1, R.id.nav_wallpaper),
    ICONS(2, R.id.nav_icons),
    MY_GALLERY(3, R.id.nav_gallery);

    companion object {
        fun fromPosition(position: Int): MainTab {
            return entries.firstOrNull { it.position == position } ?: THEMES
        }

        fun fromMenuId(menuId: Int): MainTab {
            return entries.firstOrNull { it.menuId == menuId } ?: THEMES
        }
    }
}