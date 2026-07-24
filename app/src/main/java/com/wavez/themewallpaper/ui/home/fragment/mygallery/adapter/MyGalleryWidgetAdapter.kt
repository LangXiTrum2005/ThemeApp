package com.wavez.themewallpaper.ui.home.fragment.mygallery.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.wavez.themewallpaper.core.base.BaseRecyclerViewAdapter
import com.wavez.themewallpaper.core.extensions.loadWithLoading
import com.wavez.themewallpaper.data.model.widget.WidgetsModel
import com.wavez.themewallpaper.databinding.ItemWidgetBinding

class MyGalleryWidgetAdapter : BaseRecyclerViewAdapter<WidgetsModel, ItemWidgetBinding>() {

    override fun providesItemViewBinding(parent: ViewGroup, viewType: Int): ItemWidgetBinding {
        return ItemWidgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemWidgetBinding,
        data: WidgetsModel,
        position: Int,
        context: Context,
    ) {
        binding.ivImage.loadWithLoading(data.thumb, binding.viewLoading.root)
        binding.ivVip.isVisible = false
    }
}
