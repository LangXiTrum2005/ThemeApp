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

package thinklife.prism.wallpaper.widget.theme.presentation.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseDiffCallback
import thinklife.prism.wallpaper.widget.theme.core.base.BaseListAdapter
import thinklife.prism.wallpaper.widget.theme.databinding.ItemLanguageBinding
import thinklife.prism.wallpaper.widget.theme.presentation.language.model.LanguageItem

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

    override fun inflateBinding(parent: ViewGroup, viewType: Int): ItemLanguageBinding =
        ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun bindData(binding: ItemLanguageBinding, item: LanguageItem, position: Int) {
        binding.ivFlag.setImageResource(item.flagRes)
        binding.tvName.setText(item.nameRes)
        val selected = item.matches(selectedLanguage)
        binding.ivCheck.setImageResource(
            if (selected) R.drawable.ic_language_radio_selected else R.drawable.ic_language_radio_unselected,
        )
        binding.root.setBackgroundResource(
            if (selected) R.drawable.bg_list_item_selected else R.drawable.bg_list_item,
        )
        binding.tvName.setTextColor(
            ContextCompat.getColor(
                binding.root.context,
                if (selected) R.color.accent_text else R.color.text_primary,
            ),
        )
    }
}
