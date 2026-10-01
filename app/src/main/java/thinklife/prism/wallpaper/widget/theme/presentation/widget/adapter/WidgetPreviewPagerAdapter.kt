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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.extensions.loadWithLoading
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetsModel
import thinklife.prism.wallpaper.widget.theme.databinding.ItemWidgetPreviewBinding
import kotlin.math.min

class WidgetPreviewPagerAdapter(
    private val widget: WidgetsModel,
) : RecyclerView.Adapter<WidgetPreviewPagerAdapter.WidgetPreviewViewHolder>() {

    val sizes = WidgetSize.entries

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetPreviewViewHolder =
        WidgetPreviewViewHolder(
            ItemWidgetPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: WidgetPreviewViewHolder, position: Int) {
        val size = sizes[position]
        val imageUrl = when (size) {
            WidgetSize.SMALL -> widget.contentS
            WidgetSize.MEDIUM -> widget.contentM
            WidgetSize.LARGE -> widget.contentL
        }
        holder.bind(imageUrl, size)
    }

    override fun getItemCount(): Int = sizes.size

    class WidgetPreviewViewHolder(
        private val binding: ItemWidgetPreviewBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String, widgetSize: WidgetSize) {
            binding.root.post {
                val density = binding.root.resources.displayMetrics.density
                val maxPreviewWidth = min(binding.root.width, (304 * density).toInt())
                val shortEdge = min(maxPreviewWidth, (144 * density).toInt())
                val targetWidth = when (widgetSize) {
                    WidgetSize.SMALL -> shortEdge
                    WidgetSize.MEDIUM, WidgetSize.LARGE -> maxPreviewWidth
                }
                val targetHeight = when (widgetSize) {
                    WidgetSize.SMALL, WidgetSize.MEDIUM -> shortEdge
                    WidgetSize.LARGE -> maxPreviewWidth
                }
                binding.ivPreview.layoutParams = binding.ivPreview.layoutParams.apply {
                    width = targetWidth
                    height = targetHeight
                }
                binding.viewLoading.root.layoutParams = binding.viewLoading.root.layoutParams.apply {
                    width = targetWidth
                    height = targetHeight
                }
            }

            binding.ivPreview.loadWithLoading(imageUrl, binding.viewLoading.root)
        }
    }
}

enum class WidgetSize(@param:StringRes val titleRes: Int) {
    SMALL(R.string.small),
    MEDIUM(R.string.medium),
    LARGE(R.string.large),
}
