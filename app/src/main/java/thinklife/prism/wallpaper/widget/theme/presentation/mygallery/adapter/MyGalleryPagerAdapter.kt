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

package thinklife.prism.wallpaper.widget.theme.presentation.mygallery.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.mygallery.page.MyGalleryThemesFragment
import thinklife.prism.wallpaper.widget.theme.presentation.mygallery.page.MyGalleryWallpapersFragment
import thinklife.prism.wallpaper.widget.theme.presentation.mygallery.page.MyGalleryWidgetsFragment

class MyGalleryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = PAGE_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            PAGE_THEMES -> MyGalleryThemesFragment.newInstance()
            PAGE_WALLPAPERS -> MyGalleryWallpapersFragment.newInstance()
            PAGE_WIDGETS -> MyGalleryWidgetsFragment.newInstance()
            else -> error("Unsupported My Gallery page: $position")
        }
    }

    companion object {
        const val PAGE_THEMES = 0
        const val PAGE_WALLPAPERS = 1
        const val PAGE_WIDGETS = 2
        const val PAGE_COUNT = 3
    }
}
