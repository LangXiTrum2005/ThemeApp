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

package thinklife.prism.wallpaper.widget.theme.presentation.iconpack.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import thinklife.prism.wallpaper.widget.theme.core.base.BaseRecyclerViewAdapter
import thinklife.prism.wallpaper.widget.theme.data.model.AppInfo
import thinklife.prism.wallpaper.widget.theme.databinding.ItemSelectAppBinding

class SelectAppAdapter : BaseRecyclerViewAdapter<AppInfo, ItemSelectAppBinding>() {

    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemSelectAppBinding {
        return ItemSelectAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemSelectAppBinding,
        data: AppInfo,
        position: Int,
        context: Context
    ) {
        binding.tvAppName.text = data.appName
        binding.ivAppIcon.setImageDrawable(data.icon)
    }

}
