package com.wavez.controllerwidget.core.base

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T, V> =
        BaseViewHolder(providesItemViewBinding(parent, viewType))

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
            notifyItemChanged(oldPos)
            notifyItemChanged(newPos)
            onClickItem?.invoke(data)
        }
    }

    abstract fun providesItemViewBinding(parent: ViewGroup, viewType: Int): V
    fun add(itemList: List<T>) {
        val size = this.mDataList.size
        this.mDataList.addAll(itemList)
        val sizeNew = this.mDataList.size
        notifyItemRangeChanged(size, sizeNew)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAt(position: Int, item: T) {
        mDataList.add(position, item)
        notifyDataSetChanged()
    }

    fun addAt(position: Int, itemList: List<T>) {
        val size = this.mDataList.size
        this.mDataList.addAll(position, itemList)
        val sizeNew = this.mDataList.size
        notifyItemRangeChanged(size, sizeNew)
    }

    fun setItemAt(position: Int, item: T) {
        mDataList[position] = item
        notifyItemChanged(position)
    }

    fun getItemAt(position: Int): T = mDataList[position]

    @SuppressLint("NotifyDataSetChanged")
    open fun set(dataList: List<T>, currentItem: T? = null) {
        if (currentItem != null) {
            this.currentItem = currentItem
        } else {
            if (dataList.isNotEmpty()) {
                this.currentItem = dataList[0]
            }
        }
        val clone: List<T> = ArrayList(dataList)
        listPrevious = ArrayList(mDataList)
        mDataList.clear()
        mDataList.addAll(clone)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeAt(position: Int) {
        mDataList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeAt(position: Int, b: Boolean) {
        mDataList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }

    fun removeAtPosition(position: Int) {
        if (position > -1 && position < mDataList.size) {
            mDataList.removeAt(position)
            notifyItemRemoved(position)
        }
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
