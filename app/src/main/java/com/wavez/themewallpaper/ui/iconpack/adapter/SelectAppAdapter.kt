package com.wavez.themewallpaper.ui.iconpack.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wavez.themewallpaper.core.base.BaseRecyclerViewAdapter
import com.wavez.themewallpaper.data.model.AppInfo
import com.wavez.themewallpaper.databinding.ItemSelectAppBinding

class SelectAppAdapter : BaseRecyclerViewAdapter<AppInfo, ItemSelectAppBinding>() {

    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemSelectAppBinding {
        return ItemSelectAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemSelectAppBinding,
        data: AppInfo,
        position: Int,
        context: Context
    ) {
        binding.tvAppName.text = data.appName
        binding.ivAppIcon.setImageDrawable(data.icon)
    }

}
