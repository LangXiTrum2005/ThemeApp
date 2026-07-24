package com.wavez.themewallpaper.ui.home.fragment.widget.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wavez.themewallpaper.data.model.widget.WidgetCategoryModel
import com.wavez.themewallpaper.ui.home.fragment.widget.page.WidgetCategoryFragment

class WidgetCategoryPagerAdapter(
    fragment: Fragment,
    private val categories: List<WidgetCategoryModel>,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment =
        WidgetCategoryFragment.newInstance(categories[position].id)
}
