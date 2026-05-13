package com.wavez.controllerwidget.core.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapterVer2<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val _items: MutableList<T> = mutableListOf()
    val items: List<T> get() = _items

    // Abstract method to bind data to the view holder
    abstract fun bindViewHolder(holder: RecyclerView.ViewHolder, item: T, position: Int)

    // Method to update the list of items
    @SuppressLint("NotifyDataSetChanged")
    open fun setData(newItems: List<T>) {
        _items.clear()
        _items.addAll(newItems)
        notifyDataSetChanged()
    }

    // Returns the layout resource for each item type
    abstract fun getItemLayout(viewType: Int): Int

    // Method to determine the item view type
    override fun getItemViewType(position: Int): Int = getItemType(_items[position])

    // Abstract method to return the item type based on the data
    abstract fun getItemType(item: T): Int

    // Method to create the view holder based on the view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(getItemLayout(viewType), parent, false)
        return createViewHolder(view, viewType)
    }

    // Abstract method to create the ViewHolder
    abstract fun createViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = _items[position]
        bindViewHolder(holder, item, position)
    }

    override fun getItemCount(): Int = _items.size
}
