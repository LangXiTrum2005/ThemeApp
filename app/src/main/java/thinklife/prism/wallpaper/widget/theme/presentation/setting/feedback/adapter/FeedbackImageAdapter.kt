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

package thinklife.prism.wallpaper.widget.theme.presentation.setting.feedback.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import thinklife.prism.wallpaper.widget.theme.core.base.BaseRecyclerViewAdapter
import thinklife.prism.wallpaper.widget.theme.databinding.ItemImageFeedbackBinding

class FeedbackImageAdapter : BaseRecyclerViewAdapter<Uri, ItemImageFeedbackBinding>() {

    var onRemoveItem: ((Uri) -> Unit)? = null
    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemImageFeedbackBinding {
        return ItemImageFeedbackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemImageFeedbackBinding,
        data: Uri,
        position: Int,
        context: Context
    ) {
        Glide.with(context).load(data).into(binding.imgIcon)

        binding.btnDelete.setOnClickListener {
            onRemoveItem?.invoke(data)
        }
    }
}