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

package thinklife.prism.wallpaper.widget.theme.presentation.icons.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import thinklife.prism.wallpaper.widget.theme.core.base.BaseRecyclerViewAdapter
import thinklife.prism.wallpaper.widget.theme.core.extensions.loadWithLoading
import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconPackModel
import thinklife.prism.wallpaper.widget.theme.databinding.ItemIconBinding

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

        binding.tvName.text = data.name
        binding.root.contentDescription = data.name
        binding.ivImage.isVisible = true
        binding.ivImage.loadWithLoading(data.thumb, loadingView = binding.viewLoading.root)

    }
}
