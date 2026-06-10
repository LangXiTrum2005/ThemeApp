package com.wavez.themewallpaper.ui.theme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wavez.themewallpaper.core.base.BaseRecyclerViewAdapter
import com.wavez.themewallpaper.core.extensions.loadWithLoading
import com.wavez.themewallpaper.data.model.icon.IconItemModel
import com.wavez.themewallpaper.databinding.ItemIconPreviewBinding

class AppliedThemeIconAdapter :
    BaseRecyclerViewAdapter<IconItemModel, ItemIconPreviewBinding>() {

    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int,
    ): ItemIconPreviewBinding {
        return ItemIconPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
    }

    override fun bindData(
        binding: ItemIconPreviewBinding,
        data: IconItemModel,
        position: Int,
        context: Context,
    ) {
        binding.ivIcon.loadWithLoading(data.iconUrl, null, null)
    }
}
