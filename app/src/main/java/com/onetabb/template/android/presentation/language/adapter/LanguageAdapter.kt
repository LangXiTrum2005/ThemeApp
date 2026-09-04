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

package com.onetabb.template.android.presentation.language.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.onetabb.template.android.R
import com.onetabb.template.android.core.base.BaseDiffCallback
import com.onetabb.template.android.core.base.BaseListAdapter
import com.onetabb.template.android.databinding.ItemLanguageBinding
import com.onetabb.template.android.presentation.language.model.LanguageItem

class LanguageAdapter :
    BaseListAdapter<LanguageItem, ItemLanguageBinding>(
        BaseDiffCallback(areItemsTheSameCallback = { old, new -> old.code == new.code }),
    ) {

    private var selectedLanguage = ""

    fun setSelectedLanguage(languageTag: String) {
        val oldIndex = currentList.indexOfFirst { it.matches(selectedLanguage) }
        val newIndex = currentList.indexOfFirst { it.matches(languageTag) }
        selectedLanguage = languageTag
        if (oldIndex >= 0) notifyItemChanged(oldIndex)
        if (newIndex >= 0) notifyItemChanged(newIndex)
    }

    override fun inflateBinding(parent: ViewGroup, viewType: Int): ItemLanguageBinding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun bindData(binding: ItemLanguageBinding, item: LanguageItem, position: Int) {
        binding.ivFlag.setImageResource(item.flagRes)
        binding.tvName.setText(item.nameRes)
        val selected = item.matches(selectedLanguage)
        binding.ivCheck.visibility = if (selected) View.VISIBLE else View.INVISIBLE
        binding.root.setBackgroundResource(
            if (selected) R.drawable.bg_list_item_selected else R.drawable.bg_list_item,
        )
    }
}
