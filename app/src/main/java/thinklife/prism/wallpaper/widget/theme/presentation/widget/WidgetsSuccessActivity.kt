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

package thinklife.prism.wallpaper.widget.theme.presentation.widget

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.core.extensions.getParcel
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetsModel
import thinklife.prism.wallpaper.widget.theme.databinding.ActivitySuccessWidgetsBinding
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.widget.adapter.WidgetAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.widget.adapter.WidgetSize
import thinklife.prism.wallpaper.widget.theme.presentation.widget.dialog.AddWidgetsBottomSheetDialog
import thinklife.prism.wallpaper.widget.theme.presentation.setting.dialog.RatePromptManager
import thinklife.prism.wallpaper.widget.theme.presentation.widget.adapter.ThemeWidgetSizeAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.widget.adapter.ThemeWidgetSizeItem
import thinklife.prism.wallpaper.widget.theme.presentation.widget.viewmodel.WidgetSuccessViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetsSuccessActivity : BaseActivity<ActivitySuccessWidgetsBinding>() {

    override val adsScreenId = AdsScreen.WIDGETS_SUCCESS_VIEW

    override fun bindingView(): ActivitySuccessWidgetsBinding {
        return ActivitySuccessWidgetsBinding.inflate(layoutInflater)
    }

    /** GRID: other widgets in the same category. HORIZONTAL: theme widget sizes not yet added. */
    enum class ListMode { GRID, HORIZONTAL }

    companion object {
        private const val EXTRA_WIDGET = "extra_widget"
        private const val EXTRA_IMAGE_URL = "extra_image_url"
        private const val EXTRA_SIZE = "extra_size"
        private const val EXTRA_LIST_MODE = "extra_list_mode"
        private const val GRID_SPAN_COUNT = 2

        fun newIntent(
            context: Context,
            widget: WidgetsModel,
            imageUrl: String,
            size: WidgetSize,
            listMode: ListMode = ListMode.GRID,
        ): Intent {
            return Intent(context, WidgetsSuccessActivity::class.java).apply {
                putExtra(EXTRA_WIDGET, widget)
                putExtra(EXTRA_IMAGE_URL, imageUrl)
                putExtra(EXTRA_SIZE, size.name)
                putExtra(EXTRA_LIST_MODE, listMode.name)
            }
        }
    }

    private val viewModel by viewModels<WidgetSuccessViewModel>()

    private val widget by lazy { intent.getParcel<WidgetsModel>(EXTRA_WIDGET) }
    private val imageUrl by lazy { intent.getStringExtra(EXTRA_IMAGE_URL).orEmpty() }
    private val addedSize by lazy {
        runCatching { WidgetSize.valueOf(intent.getStringExtra(EXTRA_SIZE).orEmpty()) }
            .getOrDefault(WidgetSize.MEDIUM)
    }
    private val listMode by lazy {
        runCatching { ListMode.valueOf(intent.getStringExtra(EXTRA_LIST_MODE).orEmpty()) }
            .getOrDefault(ListMode.GRID)
    }

    private val categoryAdapter by lazy { WidgetAdapter() }
    private val sizeAdapter by lazy { ThemeWidgetSizeAdapter() }

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        Glide.with(this).load(imageUrl).into(binding.ivImage)

        val model = widget
        if (model != null && savedInstanceState == null) {
            when (listMode) {
                // Only widgets added from the widget fragment flow go into My Gallery.
                ListMode.GRID -> viewModel.saveAddedWidget(model)
                // Get-theme flow only tracks which sizes have been set, not the gallery.
                ListMode.HORIZONTAL -> sharedPref.markThemeWidgetSizeAdded(model.id, addedSize.name)
            }
        }

        when (listMode) {
            ListMode.GRID -> setupCategorySuggestions()
            ListMode.HORIZONTAL -> setupThemeSizeSuggestions()
        }
        if (savedInstanceState == null) {
            RatePromptManager.showAfterSuccessfulSetup(supportFragmentManager, sharedPref)
        }
    }




    override fun initListener() {
        super.initListener()
        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }
        binding.btnHome.setOnClickListener {
            triggerAds(AdsTrigger.WIDGETS_SUCCESS_CLICK_HOME) {
                runWhenResumed {
                    navigateTo(MainActivity.newIntent(this).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                }
            }
        }
    }

    private fun setupCategorySuggestions() {
        val model = widget ?: return
        binding.rcvWidget.layoutManager = GridLayoutManager(this, GRID_SPAN_COUNT)
        binding.rcvWidget.isNestedScrollingEnabled = false
        binding.rcvWidget.adapter = categoryAdapter
        categoryAdapter.onClickItem = { selected ->
            triggerAds(AdsTrigger.WIDGETS_SUCCESS_CLICK_CATEGORY_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    AddWidgetsBottomSheetDialog.newInstance(selected)
                        .show(supportFragmentManager, AddWidgetsBottomSheetDialog.TAG)
                }
            }
        }
        viewModel.suggestedWidgets.collectWithLifecycle(this) { widgets ->
            val hasItems = widgets.isNotEmpty()
            binding.tvSuggested.isVisible = hasItems
            binding.rcvWidget.isVisible = hasItems
            categoryAdapter.set(widgets)
        }
        viewModel.loadSameCategoryWidgets(model.categoryId, model.id)
    }

    private fun setupThemeSizeSuggestions() {
        val model = widget ?: return
        val addedSizes = sharedPref.getAddedThemeWidgetSizes(model.id)
        val remaining = WidgetSize.entries
            .filter { it.name !in addedSizes }
            .map { ThemeWidgetSizeItem(it, model.contentFor(it)) }

        val hasItems = remaining.isNotEmpty()
        binding.tvSuggested.isVisible = hasItems
        binding.rcvWidget.isVisible = hasItems
        if (!hasItems) return

        binding.rcvWidget.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvWidget.isNestedScrollingEnabled = false
        binding.rcvWidget.adapter = sizeAdapter
        sizeAdapter.onClickItem = { item ->
            triggerAds(AdsTrigger.WIDGETS_SUCCESS_CLICK_SIZE_ADAPTER_CLICK_ITEM) {
                runWhenResumed {
                    AddWidgetsBottomSheetDialog.newInstance(
                        widget = model,
                        initialSize = item.size,
                        isThemeWidget = true,
                    ).show(supportFragmentManager, AddWidgetsBottomSheetDialog.TAG)
                }
            }
        }
        sizeAdapter.set(remaining)
    }

    private fun WidgetsModel.contentFor(size: WidgetSize): String = when (size) {
        WidgetSize.SMALL -> contentS
        WidgetSize.MEDIUM -> contentM
        WidgetSize.LARGE -> contentL
    }
    override fun onHandleBackPressed() {
        triggerBackAds(AdsTrigger.WIDGETS_SUCCESS_CLICK_BACK) { finish() }
    }

}
