package com.wavez.controllerwidget.ui.home.fragment.icons.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.wavez.controllerwidget.core.base.BaseRecyclerViewAdapter
import com.wavez.controllerwidget.core.extensions.loadWithLoading
import com.wavez.controllerwidget.data.model.icon.IconPackModel
import com.wavez.controllerwidget.databinding.ItemIconBinding

class IconAdapter(
    private val isApply: Boolean = false
) : BaseRecyclerViewAdapter<IconPackModel, ItemIconBinding>() {

    override fun providesItemViewBinding(parent: ViewGroup, viewType: Int): ItemIconBinding {
        return ItemIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemIconBinding,
        data: IconPackModel,
        position: Int,
        context: Context,
    ) {
        binding.tvName.isVisible = !isApply
        binding.tvViewAll.isVisible = !isApply

        binding.tvName.text = data.name
        binding.ivImage.isVisible = true
        binding.ivImage.loadWithLoading(data.thumb, loadingView = binding.viewLoading.root)

    }
}
