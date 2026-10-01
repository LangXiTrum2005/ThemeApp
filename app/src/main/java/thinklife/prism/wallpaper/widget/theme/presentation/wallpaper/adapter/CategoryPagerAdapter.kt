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

package thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import thinklife.prism.wallpaper.widget.theme.data.model.WallpaperCategoryModel
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.page.WallpaperCategoryFragment

class CategoryPagerAdapter(
    fragment: Fragment,
    private val categories: List<WallpaperCategoryModel>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return WallpaperCategoryFragment.newInstance(
            categoryId = categories[position].id
        )
    }
}