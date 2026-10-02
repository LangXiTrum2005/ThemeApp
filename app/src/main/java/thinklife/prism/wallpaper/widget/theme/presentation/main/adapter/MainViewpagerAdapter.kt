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

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.controller.ControllerFragment
import thinklife.prism.wallpaper.widget.theme.presentation.icons.IconFragment
import thinklife.prism.wallpaper.widget.theme.presentation.theme.ThemeFragment
import thinklife.prism.wallpaper.widget.theme.presentation.mygallery.MyGalleryFragment
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.WallpaperFragment
import thinklife.prism.wallpaper.widget.theme.presentation.widget.WidgetsFragment

class MainPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = MainTab.entries.size

    override fun createFragment(position: Int): Fragment =
        when (MainTab.fromPosition(position)) {
            MainTab.THEMES -> ThemeFragment.newInstance()
            MainTab.WALLPAPERS -> WallpaperFragment.newInstance()
            MainTab.WIDGETS -> WidgetsFragment.newInstance()
            MainTab.CONTROLLER -> ControllerFragment.newInstance()
            MainTab.ICONS -> IconFragment.newInstance()
            MainTab.MY_GALLERY -> MyGalleryFragment.newInstance()
        }
}
