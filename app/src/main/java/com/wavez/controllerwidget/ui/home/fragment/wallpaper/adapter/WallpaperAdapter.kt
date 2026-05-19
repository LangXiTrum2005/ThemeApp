package com.wavez.controllerwidget.ui.home.fragment.wallpaper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.wavez.controllerwidget.core.base.BaseRecyclerViewAdapter
import com.wavez.controllerwidget.core.extensions.loadWithLoading
import com.wavez.controllerwidget.data.model.WallpaperModel
import com.wavez.controllerwidget.databinding.ItemWallpaperBinding

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
        binding.tvImage.loadWithLoading(data.thumb, binding.viewLoading.root)
        binding.ivVip.isVisible = data.isVip()
    }
}
