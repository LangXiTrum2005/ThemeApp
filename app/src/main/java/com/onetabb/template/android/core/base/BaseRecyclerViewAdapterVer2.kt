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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapterVer2<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val _items: MutableList<T> = mutableListOf()
    val items: List<T> get() = _items

    abstract fun bindViewHolder(holder: RecyclerView.ViewHolder, item: T, position: Int)

    @SuppressLint("NotifyDataSetChanged")
    open fun setData(newItems: List<T>) {
        _items.clear()
        _items.addAll(newItems)
        notifyDataSetChanged()
    }

    abstract fun getItemLayout(viewType: Int): Int

    override fun getItemViewType(position: Int): Int = getItemType(_items[position])

    abstract fun getItemType(item: T): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(getItemLayout(viewType), parent, false)
        return createViewHolder(view, viewType)
    }

    abstract fun createViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindViewHolder(holder, _items[position], position)
    }

    override fun getItemCount(): Int = _items.size
}
