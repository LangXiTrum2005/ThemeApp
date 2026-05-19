package com.wavez.controllerwidget.ui.home.dialog

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.databinding.ItemPopupCategoryBinding
import com.wavez.controllerwidget.databinding.PopupFilterBinding
import kotlin.math.roundToInt

class FilterPopupHelper(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
) {

    fun show(
        anchorView: View,
        activity: Activity,
        items: List<FilterCategoryItem>,
        selectedItemId: String,
        onSelected: (FilterCategoryItem) -> Unit,
    ) {
        val binding = PopupFilterBinding.inflate(layoutInflater)
        lateinit var popup: PopupWindow

        binding.rcvCategory.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = CategoryAdapter(
                items = items,
                selectedItemId = selectedItemId,
                onSelected = { item ->
                    onSelected(item)
                    popup.dismiss()
                },
            )
        }

        popup = PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true,
        ).apply {
            elevation = dp(12).toFloat()
            setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_popup_rounded))
            isOutsideTouchable = true
        }

        binding.root.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED,
        )
        val xOffset = anchorView.width - binding.root.measuredWidth
        popup.showAsDropDown(anchorView, xOffset, dp(8))
        activity.window.setDimAmount(0.5f)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        popup.setOnDismissListener {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
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
                        if (isSelected) R.color.white else R.color.text_med_em,
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
