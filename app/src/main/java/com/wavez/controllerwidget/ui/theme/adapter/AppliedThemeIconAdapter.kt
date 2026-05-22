package com.wavez.controllerwidget.ui.theme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wavez.controllerwidget.core.base.BaseRecyclerViewAdapter
import com.wavez.controllerwidget.core.extensions.loadWithLoading
import com.wavez.controllerwidget.data.model.icon.IconItemModel
import com.wavez.controllerwidget.databinding.ItemIconPreviewBinding

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
