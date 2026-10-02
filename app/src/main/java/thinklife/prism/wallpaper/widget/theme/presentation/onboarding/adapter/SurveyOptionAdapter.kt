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

package thinklife.prism.wallpaper.widget.theme.presentation.onboarding.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import thinklife.prism.wallpaper.widget.theme.core.base.BaseDiffCallback
import thinklife.prism.wallpaper.widget.theme.core.base.BaseListAdapter
import thinklife.prism.wallpaper.widget.theme.databinding.ItemSurveyOptionBinding
import thinklife.prism.wallpaper.widget.theme.presentation.onboarding.model.SurveyOption

class SurveyOptionAdapter :
    BaseListAdapter<SurveyOption, ItemSurveyOptionBinding>(
        BaseDiffCallback(areItemsTheSameCallback = { old, new -> old.id == new.id }),
    ) {

    private val selectedIds = mutableSetOf<Int>()

    fun getSelectedIds(): Set<Int> = selectedIds

    fun toggleSelection(id: Int) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) notifyItemChanged(index)
    }

    override fun inflateBinding(parent: ViewGroup, viewType: Int): ItemSurveyOptionBinding =
        ItemSurveyOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun bindData(binding: ItemSurveyOptionBinding, item: SurveyOption, position: Int) {
        binding.tvOption.text = item.title
        if (item.iconRes != 0) {
            binding.tvOption.setCompoundDrawablesWithIntrinsicBounds(item.iconRes, 0, 0, 0)
        } else {
            binding.tvOption.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
        binding.ivOptionIcon.setImageResource(item.imageRes)
        val selected = item.id in selectedIds
        binding.root.isSelected = selected
        binding.cardContainer.isSelected = selected
        binding.tvOption.isSelected = selected
    }
}
