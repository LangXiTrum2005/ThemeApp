package com.wavez.controllerwidget.ui.iconpack.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wavez.controllerwidget.core.base.BaseRecyclerViewAdapter
import com.wavez.controllerwidget.data.model.AppInfo
import com.wavez.controllerwidget.databinding.ItemSelectAppBinding

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
