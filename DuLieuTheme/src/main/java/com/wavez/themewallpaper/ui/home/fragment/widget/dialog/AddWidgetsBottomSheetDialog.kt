package com.wavez.themewallpaper.ui.home.fragment.widget.dialog

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.setMargins
import androidx.viewpager2.widget.ViewPager2
import com.wavez.themewallpaper.R
import com.wavez.themewallpaper.core.base.BaseBottomSheetDialog
import com.wavez.themewallpaper.core.extensions.getParcel
import com.wavez.themewallpaper.core.widget.WidgetPinManager
import com.wavez.themewallpaper.data.model.widget.WidgetsModel
import com.wavez.themewallpaper.databinding.BottomSheetAddWidgetsBinding
import com.wavez.themewallpaper.ui.home.fragment.widget.adapter.WidgetPreviewPagerAdapter
import com.wavez.themewallpaper.ui.home.fragment.widget.adapter.WidgetSize
import com.wavez.themewallpaper.ui.widgets.WidgetsSuccessActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddWidgetsBottomSheetDialog : BaseBottomSheetDialog<BottomSheetAddWidgetsBinding>() {

    override fun bindingView(): BottomSheetAddWidgetsBinding =
        BottomSheetAddWidgetsBinding.inflate(layoutInflater)

    private val widget by lazy {
        requireArguments().getParcel<WidgetsModel>(ARG_WIDGET)
    }

    private val previewAdapter by lazy {
        WidgetPreviewPagerAdapter(requireNotNull(widget))
    }

    /** When true the widget comes from a theme, which selects the widget list mode. */
    private val isThemeWidget by lazy {
        requireArguments().getBoolean(ARG_IS_THEME_WIDGET, false)
    }

    private val initialSize by lazy {
        requireArguments().getString(ARG_INITIAL_SIZE)?.let { WidgetSize.valueOf(it) }
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding.tvTitle.setText(previewAdapter.sizes[position].titleRes)
            updateIndicator(position)
        }
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        binding.vpPreview.adapter = previewAdapter
        createIndicators()
        binding.tvTitle.setText(previewAdapter.sizes.first().titleRes)
        binding.vpPreview.registerOnPageChangeCallback(pageChangeCallback)

        initialSize?.let { size ->
            binding.vpPreview.setCurrentItem(previewAdapter.sizes.indexOf(size), false)
        }
    }

    override fun initListener() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnUnlock.setOnClickListener { addSelectedWidget() }
    }

    private fun addSelectedWidget() {
        val model = requireNotNull(widget)
        val size = previewAdapter.sizes[binding.vpPreview.currentItem]
        val imageUrl = when (size) {
            WidgetSize.SMALL -> model.contentS
            WidgetSize.MEDIUM -> model.contentM
            WidgetSize.LARGE -> model.contentL
        }
        val listMode = if (isThemeWidget) {
            WidgetsSuccessActivity.ListMode.HORIZONTAL
        } else {
            WidgetsSuccessActivity.ListMode.GRID
        }
        val successCallback = PendingIntent.getActivity(
            requireContext(),
            size.ordinal,
            WidgetsSuccessActivity.newIntent(requireContext(), model, imageUrl, size, listMode)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val requested = WidgetPinManager.requestPin(requireContext(), size, imageUrl, successCallback)
        if (requested) {
            dismiss()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.widget_pin_not_supported,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun createIndicators() {
        val dotSize = resources.getDimensionPixelSize(R.dimen.widget_indicator_size)
        val dotMargin = resources.getDimensionPixelSize(R.dimen.widget_indicator_margin)

        previewAdapter.sizes.forEachIndexed { index, _ ->
            binding.viewIndicator.addView(
                ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                        setMargins(dotMargin)
                    }
                    setBackgroundResource(
                        if (index == 0) R.drawable.bg_widget_indicator_active
                        else R.drawable.bg_widget_indicator_inactive
                    )
                }
            )
        }
    }

    private fun updateIndicator(selectedPosition: Int) {
        repeat(binding.viewIndicator.childCount) { index ->
            binding.viewIndicator.getChildAt(index).setBackgroundResource(
                if (index == selectedPosition) R.drawable.bg_widget_indicator_active
                else R.drawable.bg_widget_indicator_inactive
            )
        }
    }

    override fun release() {
        binding.vpPreview.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.vpPreview.adapter = null
        super.release()
    }

    companion object {
        const val TAG = "AddWidgetsBottomSheetDialog"
        private const val ARG_WIDGET = "arg_widget"
        private const val ARG_INITIAL_SIZE = "arg_initial_size"
        private const val ARG_IS_THEME_WIDGET = "arg_is_theme_widget"

        fun newInstance(
            widget: WidgetsModel,
            initialSize: WidgetSize? = null,
            isThemeWidget: Boolean = false,
        ) = AddWidgetsBottomSheetDialog().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_WIDGET, widget)
                putString(ARG_INITIAL_SIZE, initialSize?.name)
                putBoolean(ARG_IS_THEME_WIDGET, isThemeWidget)
            }
        }
    }
}
