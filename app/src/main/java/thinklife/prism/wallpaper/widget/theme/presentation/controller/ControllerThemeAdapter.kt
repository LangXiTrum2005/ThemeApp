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

package thinklife.prism.wallpaper.widget.theme.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.ControllerTheme
import thinklife.prism.wallpaper.widget.theme.databinding.ItemControllerThemeBinding

class ControllerThemeAdapter(
    private val onThemeSelected: (ControllerTheme) -> Unit,
) : RecyclerView.Adapter<ControllerThemeAdapter.ViewHolder>() {

    private var themes: List<ControllerTheme> = emptyList()
    private var appliedThemeId: String? = null

    fun submit(themes: List<ControllerTheme>, selectedThemeId: String?) {
        this.themes = themes
        appliedThemeId = selectedThemeId
        notifyDataSetChanged()
    }

    fun markApplied(theme: ControllerTheme) {
        appliedThemeId = theme.id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemControllerThemeBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val theme = themes[position]
        holder.binding.root.isSelected = theme.id == appliedThemeId
        holder.binding.tvThemeName.text = theme.name
        Glide.with(holder.binding.ivTheme)
            .load(theme.thumbnail)
            .centerCrop()
            .into(holder.binding.ivTheme)
        holder.binding.root.setOnClickListener {
            onThemeSelected(theme)
        }
    }

    override fun getItemCount(): Int = themes.size

    class ViewHolder(val binding: ItemControllerThemeBinding) : RecyclerView.ViewHolder(binding.root)
}
