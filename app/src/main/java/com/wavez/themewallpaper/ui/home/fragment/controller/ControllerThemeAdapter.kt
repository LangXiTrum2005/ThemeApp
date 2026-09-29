package com.wavez.themewallpaper.ui.home.fragment.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.controllercenter.ControllerTheme
import com.wavez.themewallpaper.databinding.ItemControllerThemeBinding

enum class ControllerThemeAction {
    SELECT,
    DOWNLOAD,
    UNLOCK,
}

class ControllerThemeAdapter(
    private val onThemeAction: (ControllerTheme, ControllerThemeAction) -> Unit,
) : RecyclerView.Adapter<ControllerThemeAdapter.ViewHolder>() {

    private var themes: List<ControllerTheme> = emptyList()
    private var appliedThemeId: String? = null
    private var actionThemeId: String? = null
    private var actionState: ActionState? = null

    private enum class ActionState {
        DOWNLOAD,
        LOADING,
        UNLOCK,
    }

    fun submit(themes: List<ControllerTheme>, selectedThemeId: String?) {
        this.themes = themes
        appliedThemeId = selectedThemeId
        notifyDataSetChanged()
    }

    fun showDownload(theme: ControllerTheme) = updateAction(theme, ActionState.DOWNLOAD)

    fun showLoading(theme: ControllerTheme) = updateAction(theme, ActionState.LOADING)

    fun showUnlock(theme: ControllerTheme) = updateAction(theme, ActionState.UNLOCK)

    fun markApplied(theme: ControllerTheme) {
        appliedThemeId = theme.id
        actionThemeId = null
        actionState = null
        notifyDataSetChanged()
    }

    private fun updateAction(theme: ControllerTheme, state: ActionState) {
        actionThemeId = theme.id
        actionState = state
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemControllerThemeBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val theme = themes[position]
        val isActionTheme = theme.id == actionThemeId
        holder.binding.root.isSelected = theme.id == appliedThemeId || isActionTheme
        holder.binding.tvThemeName.text = theme.name
        Glide.with(holder.binding.ivTheme)
            .load(theme.thumbnail)
            .centerCrop()
            .into(holder.binding.ivTheme)
        holder.binding.progressThemeAction.visibility =
            if (isActionTheme && actionState == ActionState.LOADING) View.VISIBLE else View.GONE
        holder.binding.btnThemeAction.visibility =
            if (isActionTheme && actionState != ActionState.LOADING) View.VISIBLE else View.GONE
        holder.binding.btnThemeAction.text = when (actionState) {
            ActionState.DOWNLOAD -> holder.binding.root.context.getString(R.string.download)
            ActionState.UNLOCK -> holder.binding.root.context.getString(R.string.unlock)
            else -> ""
        }
        holder.binding.root.setOnClickListener {
            onThemeAction(theme, ControllerThemeAction.SELECT)
        }
        holder.binding.btnThemeAction.setOnClickListener {
            onThemeAction(
                theme,
                if (actionState == ActionState.UNLOCK) {
                    ControllerThemeAction.UNLOCK
                } else {
                    ControllerThemeAction.DOWNLOAD
                },
            )
        }
    }

    override fun getItemCount(): Int = themes.size

    class ViewHolder(val binding: ItemControllerThemeBinding) : RecyclerView.ViewHolder(binding.root)
}
