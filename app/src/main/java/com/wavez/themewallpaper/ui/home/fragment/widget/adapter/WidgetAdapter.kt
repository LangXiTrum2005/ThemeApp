package com.wavez.themewallpaper.ui.home.fragment.widget.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.wavez.themewallpaper.core.base.BaseRecyclerViewAdapter
import com.wavez.themewallpaper.core.extensions.loadWithLoading
import com.wavez.themewallpaper.data.model.widget.WidgetsModel
import com.wavez.themewallpaper.databinding.ItemWallpaperBinding
import com.wavez.themewallpaper.databinding.ItemWidgetBinding
import com.wavez.themewallpaper.di.AppModuleEntry
import dagger.hilt.android.EntryPointAccessors

class WidgetAdapter : BaseRecyclerViewAdapter<WidgetsModel, ItemWidgetBinding>() {

    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int,
    ): ItemWidgetBinding = ItemWidgetBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false,
    )

    override fun bindData(
        binding: ItemWidgetBinding,
        data: WidgetsModel,
        position: Int,
        context: Context,
    ) {
        val sharedPref = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppModuleEntry::class.java,
        ).getSharedPref()
        binding.ivImage.loadWithLoading(data.thumb, binding.viewLoading.root)
        binding.ivVip.isVisible = data.isVip() && !sharedPref.isVip
    }
}
