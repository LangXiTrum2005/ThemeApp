/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.presentation.widget.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import thinklife.prism.wallpaper.widget.theme.core.base.BaseRecyclerViewAdapter
import thinklife.prism.wallpaper.widget.theme.core.extensions.loadWithLoading
import thinklife.prism.wallpaper.widget.theme.databinding.ItemWidgetSuggestBinding
import thinklife.prism.wallpaper.widget.theme.presentation.widget.adapter.WidgetSize

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
    }
}
