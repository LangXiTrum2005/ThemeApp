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

package com.onetabb.template.android.presentation.onboarding.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.onetabb.template.android.R
import com.onetabb.template.android.core.base.BaseDiffCallback
import com.onetabb.template.android.core.base.BaseListAdapter
import com.onetabb.template.android.databinding.ItemSurveyOptionBinding
import com.onetabb.template.android.presentation.onboarding.model.SurveyOption

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

    override fun inflateBinding(parent: ViewGroup, viewType: Int): ItemSurveyOptionBinding = ItemSurveyOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun bindData(binding: ItemSurveyOptionBinding, item: SurveyOption, position: Int) {
        binding.tvOption.text = item.title
        binding.ivOptionIcon.setImageResource(item.iconRes)
        val selected = item.id in selectedIds
        binding.ivOptionIcon.isSelected = selected
        binding.ivOptionIcon.setColorFilter(
            ContextCompat.getColor(
                binding.root.context,
                if (selected) R.color.bg_main else R.color.accent,
            ),
        )
        binding.ivCheck.visibility = if (selected) View.VISIBLE else View.INVISIBLE
        binding.root.setBackgroundResource(
            if (selected) R.drawable.bg_list_item_selected else R.drawable.bg_list_item,
        )
    }
}
