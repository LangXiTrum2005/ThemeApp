package com.wavez.themewallpaper.ui.home.fragment.theme.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wavez.themewallpaper.data.model.theme.ThemeCategoryModel
import com.wavez.themewallpaper.ui.home.fragment.theme.page.ThemeCategoryFragment

class ThemeCategoryPagerAdapter(
    fragment: Fragment,
    private val categories: List<ThemeCategoryModel>,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return ThemeCategoryFragment.newInstance(categories[position].id)
    }
}
