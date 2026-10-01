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

package thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.walkthrough.model.Habits
import thinklife.prism.wallpaper.widget.theme.core.base.BaseRecyclerViewAdapter
import thinklife.prism.wallpaper.widget.theme.core.extensions.setDrawableStart
import thinklife.prism.wallpaper.widget.theme.databinding.ItemHabitsBinding

class HabitsAdapter : BaseRecyclerViewAdapter<Habits, ItemHabitsBinding>() {

    private var habitsSelects = mutableListOf<Habits>()

    var onItemSelected: (() -> Unit)? = null

    override fun providesItemViewBinding(parent: ViewGroup, viewType: Int): ItemHabitsBinding {
        return ItemHabitsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemHabitsBinding,
        data: Habits,
        position: Int,
        context: Context
    ) {
        Glide.with(context).load(data.image).into(binding.imageHabit)
        binding.tvHabitsName.text = context.getString(data.name)
        binding.tvHabitsName.setDrawableStart(data.icon)

        binding.root.setOnClickListener {
            if (habitsSelects.contains(data)) {
                habitsSelects.remove(data)
                binding.root.isSelected = false
            } else {
                habitsSelects.add(data)
                binding.root.isSelected = true
            }
            onItemSelected?.invoke()
        }
    }

    fun getSelectedItems(): List<Habits> {
        return habitsSelects
    }

}
