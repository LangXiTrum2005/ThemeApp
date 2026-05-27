package com.wavez.controllerwidget.ui.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wavez.controllerwidget.ui.home.fragment.icons.IconFragment
import com.wavez.controllerwidget.ui.home.fragment.theme.ThemeFragment
import com.wavez.controllerwidget.ui.home.fragment.mygallery.MyGalleryFragment
import com.wavez.controllerwidget.ui.home.fragment.wallpaper.WallpaperFragment

class MainPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = MainTab.entries.size

    override fun createFragment(position: Int): Fragment =
        when (MainTab.fromPosition(position)) {
            MainTab.THEMES -> ThemeFragment.newInstance()
            MainTab.WALLPAPERS -> WallpaperFragment.newInstance()
            MainTab.ICONS -> IconFragment.newInstance()
            MainTab.MY_GALLERY -> MyGalleryFragment.newInstance()
        }
}
