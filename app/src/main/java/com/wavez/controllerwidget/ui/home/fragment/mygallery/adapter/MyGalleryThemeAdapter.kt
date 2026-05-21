package com.wavez.controllerwidget.ui.home.fragment.mygallery.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.wavez.controllerwidget.core.base.BaseRecyclerViewAdapter
import com.wavez.controllerwidget.core.extensions.loadWithLoading
import com.wavez.controllerwidget.data.model.ControllerModel
import com.wavez.controllerwidget.databinding.ItemIconBinding

class MyGalleryThemeAdapter : BaseRecyclerViewAdapter<ControllerModel, ItemIconBinding>() {

    override fun providesItemViewBinding(parent: ViewGroup, viewType: Int): ItemIconBinding {
        return ItemIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemIconBinding,
        data: ControllerModel,
        position: Int,
        context: Context,
    ) {
        binding.tvName.isVisible = false
        binding.tvViewAll.isVisible = false
        binding.ivImage.loadWithLoading(data.thumb, binding.viewLoading.root)
    }
}
