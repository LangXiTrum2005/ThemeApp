package com.wavez.themewallpaper.ui.widgets.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.wavez.themewallpaper.core.base.BaseRecyclerViewAdapter
import com.wavez.themewallpaper.core.extensions.loadWithLoading
import com.wavez.themewallpaper.databinding.ItemWidgetSuggestBinding
import com.wavez.themewallpaper.ui.home.fragment.widget.adapter.WidgetSize

/** One selectable size of a theme's widget (the sizes the user hasn't added yet). */
data class ThemeWidgetSizeItem(val size: WidgetSize, val imageUrl: String)

class ThemeWidgetSizeAdapter :
    BaseRecyclerViewAdapter<ThemeWidgetSizeItem, ItemWidgetSuggestBinding>() {

    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int,
    ): ItemWidgetSuggestBinding = ItemWidgetSuggestBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false,
    )

    override fun bindData(
        binding: ItemWidgetSuggestBinding,
        data: ThemeWidgetSizeItem,
        position: Int,
        context: Context,
    ) {
        binding.ivImage.scaleType = ImageView.ScaleType.FIT_CENTER
        binding.ivImage.loadWithLoading(data.imageUrl, binding.viewLoading.root)
        binding.ivVip.isVisible = false
    }
}
