package com.wavez.themewallpaper.ui.home.fragment.mygallery.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.wavez.themewallpaper.core.base.BaseRecyclerViewAdapter
import com.wavez.themewallpaper.core.extensions.loadWithLoading
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel
import com.wavez.themewallpaper.databinding.ItemWallpaperBinding

class MyGalleryWallpaperAdapter : BaseRecyclerViewAdapter<WallpaperModel, ItemWallpaperBinding>() {

    override fun providesItemViewBinding(parent: ViewGroup, viewType: Int): ItemWallpaperBinding {
        return ItemWallpaperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemWallpaperBinding,
        data: WallpaperModel,
        position: Int,
        context: Context,
    ) {
        binding.ivImage.loadWithLoading(data.thumb, binding.viewLoading.root)
        binding.ivVip.isVisible = false
    }
}
