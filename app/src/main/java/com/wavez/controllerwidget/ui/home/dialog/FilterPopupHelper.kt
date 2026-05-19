package com.wavez.controllerwidget.ui.home.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.data.model.WallpaperCategoryModel
import com.wavez.controllerwidget.databinding.PopupFilterBinding

class FilterPopupHelper(
    private val context: Context,
    private val layoutInflater: LayoutInflater
) {

    fun show(
        anchorView: View,
        activity: Activity,
        dimOverlay: View,
        categories: List<WallpaperCategoryModel>,
        selectedCategory: WallpaperCategoryModel,
        onSelected: (String) -> Unit
    ) {
        val binding = PopupFilterBinding.inflate(layoutInflater)

        categories.forEach { category ->
            val chip = Chip(context).apply {
                text = category.name
                isCheckable = true
                isChecked = (category == selectedCategory)
                setBackgroundResource(R.drawable.bg_tab_selector)
                setTextColor(ContextCompat.getColor(context, R.color.title_tab_color))
            }
            binding.popupChipGroup.addView(chip)
        }

        dimOverlay.visibility = View.VISIBLE
        dimOverlay.alpha = 0f
        dimOverlay.animate().alpha(1f).setDuration(180).start()

        // ✅ Dim đúng cách: dùng FLAG_DIM_BEHIND trên window của popup
        val popup = PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 12f
            setBackgroundDrawable(
                ContextCompat.getDrawable(context, R.drawable.bg_popup_rounded)
            )

            dimOverlay.animate()
                .alpha(0f)
                .setDuration(180)
                .withEndAction { dimOverlay.visibility = View.GONE }
                .start()
        }

        // Gắn dim SAU khi showAsDropDown
        popup.showAsDropDown(anchorView, -200, 8)

        // ✅ Lấy window token của popup và set dim
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val token = binding.root.applicationWindowToken
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            // Dim background của chính popup window
        }

        // Cách đúng nhất: set dimAmount trên window
        activity.window.setDimAmount(0.5f)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        popup.setOnDismissListener {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        binding.popupChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = group.findViewById<Chip>(
                checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            )
            onSelected(chip.text.toString())
            popup.dismiss()
        }
    }
}