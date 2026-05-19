package com.wavez.controllerwidget.ui.home.fragment.wallpaper.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wavez.controllerwidget.data.model.WallpaperCategoryModel
import com.wavez.controllerwidget.ui.home.fragment.wallpaper.page.WallpaperCategoryFragment

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