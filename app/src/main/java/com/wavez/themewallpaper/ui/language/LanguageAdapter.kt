package com.wavez.themewallpaper.ui.language

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wavez.themewallpaper.core.base.BaseRecyclerViewAdapter
import com.wavez.themewallpaper.data.model.LanguageModel
import com.wavez.themewallpaper.databinding.ItemLanguageBinding

class LanguageAdapter : BaseRecyclerViewAdapter<LanguageModel, ItemLanguageBinding>() {

    var onSelected: ((LanguageModel) -> Unit)? = null

    var isCodeSelected: String? = null

    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemLanguageBinding {
        return ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemLanguageBinding,
        data: LanguageModel,
        position: Int,
        context: Context
    ) {
        binding.ivFlag.setImageResource(data.flag)
        binding.tvLanguage.text = data.name
        binding.tvLanguageEn.text = data.englishName
        val isSelected = data.code == isCodeSelected
        binding.root.isSelected = isSelected
        binding.ivRadioCheck.isSelected = isSelected

        binding.root.setOnClickListener {
            isCodeSelected = data.code
            onSelected?.invoke(data)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onLanguageSelected(code: String) {
        isCodeSelected = code
        notifyDataSetChanged()
    }

}