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

package thinklife.prism.wallpaper.widget.theme.presentation.theme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import thinklife.prism.wallpaper.widget.theme.core.base.BaseRecyclerViewAdapter
import thinklife.prism.wallpaper.widget.theme.core.extensions.loadWithLoading
import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconItemModel
import thinklife.prism.wallpaper.widget.theme.databinding.ItemIconPreviewBinding

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
