package com.wavez.themewallpaper.ui.home.fragment.theme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wavez.themewallpaper.core.base.BaseRecyclerViewAdapter
import com.wavez.themewallpaper.core.extensions.loadWithLoading
import com.wavez.themewallpaper.data.model.theme.ThemeModel
import com.wavez.themewallpaper.databinding.ItemThemeBinding

class ThemeAdapter : BaseRecyclerViewAdapter<ThemeModel, ItemThemeBinding>() {

    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int,
    ): ItemThemeBinding {
        return ItemThemeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
    }

    override fun bindData(
        binding: ItemThemeBinding,
        data: ThemeModel,
        position: Int,
        context: Context,
    ) {
        binding.ivImage.loadWithLoading(data.thumb, binding.viewLoading.root)
    }
}
