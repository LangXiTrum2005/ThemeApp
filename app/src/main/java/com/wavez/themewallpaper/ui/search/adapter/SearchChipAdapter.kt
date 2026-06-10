package com.wavez.themewallpaper.ui.search.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wavez.themewallpaper.core.base.BaseRecyclerViewAdapter
import com.wavez.themewallpaper.databinding.ItemSearchChipBinding
import com.wavez.themewallpaper.ui.search.model.SearchCategoryUi

class SearchChipAdapter : BaseRecyclerViewAdapter<SearchCategoryUi, ItemSearchChipBinding>() {

    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int,
    ): ItemSearchChipBinding {
        return ItemSearchChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemSearchChipBinding,
        data: SearchCategoryUi,
        position: Int,
        context: Context,
    ) {
        binding.root.text = data.name
    }
}