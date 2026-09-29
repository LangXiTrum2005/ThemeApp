package com.wavez.themewallpaper.ui.home.fragment.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wavez.themewallpaper.core.controllercenter.ControllerTheme
import com.wavez.themewallpaper.databinding.ItemControllerThemeBinding

class ControllerThemeAdapter(
    private val onThemeSelected: (ControllerTheme) -> Unit,
) : RecyclerView.Adapter<ControllerThemeAdapter.ViewHolder>() {

    private var themes: List<ControllerTheme> = emptyList()
    private var selectedThemeId: String? = null

    fun submit(themes: List<ControllerTheme>, selectedThemeId: String?) {
        this.themes = themes
        this.selectedThemeId = selectedThemeId
        notifyDataSetChanged()
    }

    fun select(theme: ControllerTheme) {
        val previousPosition = themes.indexOfFirst { it.id == selectedThemeId }
        selectedThemeId = theme.id
        if (previousPosition >= 0) notifyItemChanged(previousPosition)
        notifyItemChanged(themes.indexOf(theme))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemControllerThemeBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val theme = themes[position]
        holder.binding.root.isSelected = theme.id == selectedThemeId
        holder.binding.tvThemeName.text = theme.name
        Glide.with(holder.binding.ivTheme)
            .load(theme.thumbnail)
            .centerCrop()
            .into(holder.binding.ivTheme)
        holder.binding.root.setOnClickListener { onThemeSelected(theme) }
    }

    override fun getItemCount(): Int = themes.size

    class ViewHolder(val binding: ItemControllerThemeBinding) : RecyclerView.ViewHolder(binding.root)
}
