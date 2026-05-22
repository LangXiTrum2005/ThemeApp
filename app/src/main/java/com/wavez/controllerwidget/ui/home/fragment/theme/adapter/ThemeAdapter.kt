package com.wavez.controllerwidget.ui.home.fragment.theme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.wavez.controllerwidget.core.base.BaseRecyclerViewAdapter
import com.wavez.controllerwidget.core.extensions.loadWithLoading
import com.wavez.controllerwidget.data.model.theme.ThemeModel
import com.wavez.controllerwidget.databinding.ItemThemeBinding

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
        binding.ivVip.isVisible = data.isVip()
    }
}
