package com.wavez.themewallpaper.ui.home.fragment.theme.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wavez.themewallpaper.core.extensions.loadWithLoading
import com.wavez.themewallpaper.data.model.theme.ThemeCategoryModel
import com.wavez.themewallpaper.databinding.ItemThemeBannerBinding

class ThemeBannerAdapter : RecyclerView.Adapter<ThemeBannerAdapter.BannerViewHolder>() {

    companion object {
        private const val MIN_INFINITE_COUNT = 3
    }

    private val categories = mutableListOf<ThemeCategoryModel>()
    var onClickItem: ((ThemeCategoryModel) -> Unit)? = null

    val realCount: Int
        get() = categories.size

    @SuppressLint("NotifyDataSetChanged")
    fun submit(items: List<ThemeCategoryModel>): Boolean {
        if (categories == items) return false
        categories.clear()
        categories.addAll(items)
        notifyDataSetChanged()
        return true
    }

    fun realPosition(position: Int): Int {
        if (categories.isEmpty()) return 0
        return position.floorMod(categories.size)
    }

    override fun getItemCount(): Int {
        return if (categories.size >= MIN_INFINITE_COUNT) {
            Int.MAX_VALUE
        } else {
            categories.size
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        return BannerViewHolder(
            ItemThemeBannerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val realPosition = realPosition(position)
        val category = categories[realPosition]
        holder.bind(category)
        holder.itemView.setOnClickListener {
            onClickItem?.invoke(category)
        }
    }

    class BannerViewHolder(
        private val binding: ItemThemeBannerBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: ThemeCategoryModel) {
            binding.ivBanner.loadWithLoading(category.banner, binding.viewLoading.root)
        }
    }

    private fun Int.floorMod(other: Int): Int {
        return ((this % other) + other) % other
    }
}
