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

package com.onetabb.template.android.core.base

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseListAdapter<T : Any, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>,
) : androidx.recyclerview.widget.ListAdapter<T, BaseViewHolder<T, VB>>(diffCallback) {

    var onItemClick: ((item: T, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T, VB> = BaseViewHolder(inflateBinding(parent, viewType))

    override fun onBindViewHolder(holder: BaseViewHolder<T, VB>, position: Int) {
        val item = getItem(position)
        holder.binding.root.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onItemClick?.invoke(item, pos)
            }
        }
        bindData(holder.binding, item, position)
    }

    protected abstract fun inflateBinding(parent: ViewGroup, viewType: Int): VB

    protected abstract fun bindData(binding: VB, item: T, position: Int)
}
