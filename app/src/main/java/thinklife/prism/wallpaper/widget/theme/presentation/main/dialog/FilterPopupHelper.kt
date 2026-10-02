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

package thinklife.prism.wallpaper.widget.theme.presentation.main.dialog

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.databinding.ItemPopupCategoryBinding
import thinklife.prism.wallpaper.widget.theme.databinding.PopupFilterBinding
import kotlin.math.roundToInt

class FilterPopupHelper(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
) {

    private var currentPopup: PopupWindow? = null
    private var lastDismissTimeMs: Long = 0L

    val isShowing: Boolean
        get() = currentPopup?.isShowing == true

    fun dismiss() {
        try {
            if (currentPopup?.isShowing == true) {
                currentPopup?.dismiss()
            }
        } catch (_: Exception) {
        } finally {
            currentPopup = null
        }
    }

    fun show(
        anchorView: View,
        activity: Activity,
        items: List<FilterCategoryItem>,
        selectedItemId: String,
        onSelected: (FilterCategoryItem) -> Unit,
    ) {
        if (isShowing) {
            dismiss()
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastDismissTimeMs < 300L) {
            return
        }

        val binding = PopupFilterBinding.inflate(layoutInflater)

        binding.rcvCategory.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = CategoryAdapter(
                items = items,
                selectedItemId = selectedItemId,
                onSelected = { item ->
                    onSelected(item)
                    dismiss()
                },
            )
        }

        val popup = PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true,
        ).apply {
            elevation = dp(12).toFloat()
            setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_popup_rounded))
            isOutsideTouchable = true
        }
        currentPopup = popup

        binding.root.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED,
        )
        val xOffset = anchorView.width - binding.root.measuredWidth
        popup.showAsDropDown(anchorView, xOffset, dp(8))
        hideNavigationBar(activity)
        try {
            activity.window.setDimAmount(0.5f)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        } catch (_: Exception) {
        }

        popup.setOnDismissListener {
            lastDismissTimeMs = System.currentTimeMillis()
            currentPopup = null
            try {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                hideNavigationBar(activity)
            } catch (_: Exception) {
            }
        }
    }

    private fun hideNavigationBar(activity: Activity) {
        try {
            WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.navigationBars())
            }
        } catch (_: Exception) {
        }
    }

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).roundToInt()
    }

    private inner class CategoryAdapter(
        private val items: List<FilterCategoryItem>,
        private val selectedItemId: String,
        private val onSelected: (FilterCategoryItem) -> Unit,
    ) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            return CategoryViewHolder(
                ItemPopupCategoryBinding.inflate(layoutInflater, parent, false),
            )
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            holder.bind(items[position])
        }

        private inner class CategoryViewHolder(
            private val binding: ItemPopupCategoryBinding,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: FilterCategoryItem) {
                val isSelected = item.id == selectedItemId
                binding.container.setBackgroundResource(
                    if (isSelected) R.drawable.bg_tab_selected else R.drawable.bg_tab_unselected,
                )
                binding.tvTitle.text = item.title
                binding.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (isSelected) R.color.prism_tab_active_text else R.color.text_med_em,
                    ),
                )
                if (item.iconUrl.isNullOrEmpty()) {
                    binding.ivIcon.isVisible = false
                } else {
                    binding.ivIcon.isVisible = true
                    Glide.with(binding.ivIcon)
                        .load(item.iconUrl)
                        .into(binding.ivIcon)
                }
                binding.root.setOnClickListener {
                    onSelected(item)
                }

            }
        }
    }
}
