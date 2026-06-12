package com.wavez.themewallpaper.ui.home.fragment.wallpaper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.wavez.themewallpaper.core.base.BaseRecyclerViewAdapter
import com.wavez.themewallpaper.core.extensions.loadWithLoading
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel
import com.wavez.themewallpaper.databinding.ItemWallpaperBinding
import com.wavez.themewallpaper.di.AppModuleEntry
import dagger.hilt.android.EntryPointAccessors

class WallpaperAdapter : BaseRecyclerViewAdapter<WallpaperModel, ItemWallpaperBinding>() {
    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemWallpaperBinding {
        return ItemWallpaperBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    }

    override fun bindData(
        binding: ItemWallpaperBinding,
        data: WallpaperModel,
        position: Int,
        context: Context
    ) {
        val sharedPref = EntryPointAccessors.fromApplication(
            context.applicationContext, AppModuleEntry::class.java
        ).getSharedPref()
        binding.ivImage.loadWithLoading(data.thumb, binding.viewLoading.root)
        binding.ivVip.isVisible = data.isVip() && !sharedPref.isVip
    }
}
