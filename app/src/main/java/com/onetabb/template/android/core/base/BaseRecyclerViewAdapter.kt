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
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseRecyclerViewAdapter<T, V : ViewBinding>(
    val mDataList: MutableList<T> = mutableListOf(),
) : RecyclerView.Adapter<BaseViewHolder<T, V>>() {

    var listPrevious = arrayListOf<T>()
    var onClickItem: ((T) -> Unit)? = null
    var currentItem: T? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T, V> = BaseViewHolder(providesItemViewBinding(parent, viewType))

    override fun onBindViewHolder(holder: BaseViewHolder<T, V>, position: Int) {
        val data = mDataList[position]
        onItemSelect(holder, data)
        bindData(holder.binding, data, position, holder.binding.root.context)
    }

    open fun onItemSelect(holder: BaseViewHolder<T, V>, data: T) {
        holder.binding.root.setOnClickListener {
            val oldPos = mDataList.indexOf(currentItem)
            val newPos = mDataList.indexOf(data)
            currentItem = data
            if (oldPos != RecyclerView.NO_POSITION) notifyItemChanged(oldPos)
            if (newPos != RecyclerView.NO_POSITION) notifyItemChanged(newPos)
            onClickItem?.invoke(data)
        }
    }

    abstract fun providesItemViewBinding(parent: ViewGroup, viewType: Int): V

    fun add(itemList: List<T>) {
        if (itemList.isEmpty()) return
        val startPosition = mDataList.size
        mDataList.addAll(itemList)
        notifyItemRangeInserted(startPosition, itemList.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAt(position: Int, item: T) {
        mDataList.add(position, item)
        notifyDataSetChanged()
    }

    fun addAt(position: Int, itemList: List<T>) {
        if (itemList.isEmpty()) return
        val safePosition = position.coerceIn(0, mDataList.size)
        mDataList.addAll(safePosition, itemList)
        notifyItemRangeInserted(safePosition, itemList.size)
    }

    fun setItemAt(position: Int, item: T) {
        mDataList[position] = item
        notifyItemChanged(position)
    }

    fun getItemAt(position: Int): T = mDataList[position]

    @SuppressLint("NotifyDataSetChanged")
    open fun set(dataList: List<T>, currentItem: T? = null) {
        this.currentItem = currentItem ?: dataList.firstOrNull()
        val clone: List<T> = ArrayList(dataList)
        listPrevious = ArrayList(mDataList)
        mDataList.clear()
        mDataList.addAll(clone)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        mDataList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeAt(position: Int, notifyRangeChanged: Boolean) {
        if (position !in mDataList.indices) return
        mDataList.removeAt(position)
        notifyItemRemoved(position)
        if (notifyRangeChanged) notifyItemRangeChanged(position, itemCount - position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        mDataList.clear()
        notifyDataSetChanged()
    }

    fun backPreviousList(): Boolean {
        if (listPrevious.size > 0) {
            set(listPrevious)
            listPrevious = arrayListOf()
            return true
        }
        return false
    }

    abstract fun bindData(binding: V, data: T, position: Int, context: Context)

    override fun getItemCount(): Int = mDataList.size
}
