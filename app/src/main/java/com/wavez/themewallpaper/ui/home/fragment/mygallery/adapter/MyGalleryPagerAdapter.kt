package com.wavez.themewallpaper.ui.home.fragment.mygallery.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wavez.themewallpaper.ui.home.fragment.mygallery.page.MyGalleryThemesFragment
import com.wavez.themewallpaper.ui.home.fragment.mygallery.page.MyGalleryWallpapersFragment
import com.wavez.themewallpaper.ui.home.fragment.mygallery.page.MyGalleryWidgetsFragment

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
