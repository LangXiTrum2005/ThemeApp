package com.wavez.controllerwidget.ui.setting.feedback.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.wavez.controllerwidget.core.base.BaseRecyclerViewAdapter
import com.wavez.controllerwidget.databinding.ItemImageFeedbackBinding

class FeedbackImageAdapter : BaseRecyclerViewAdapter<Uri, ItemImageFeedbackBinding>() {

    var onRemoveItem: ((Uri) -> Unit)? = null
    override fun providesItemViewBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemImageFeedbackBinding {
        return ItemImageFeedbackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemImageFeedbackBinding,
        data: Uri,
        position: Int,
        context: Context
    ) {
        Glide.with(context).load(data).into(binding.imgIcon)

        binding.btnDelete.setOnClickListener {
            onRemoveItem?.invoke(data)
        }
    }
}