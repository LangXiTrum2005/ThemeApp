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

package thinklife.prism.wallpaper.widget.theme.presentation.theme.activities

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.core.base.BaseActivity
import thinklife.prism.wallpaper.widget.theme.core.base.UiState
import thinklife.prism.wallpaper.widget.theme.core.download.DownloadState
import thinklife.prism.wallpaper.widget.theme.core.extensions.animateSegmentSelected
import thinklife.prism.wallpaper.widget.theme.core.extensions.collectWithLifecycle
import thinklife.prism.wallpaper.widget.theme.core.extensions.getParcel
import thinklife.prism.wallpaper.widget.theme.core.extensions.loadWithLoading
import thinklife.prism.wallpaper.widget.theme.core.icons.IconShortcutInstaller
import thinklife.prism.wallpaper.widget.theme.core.wallpaper.model.WallpaperTarget
import thinklife.prism.wallpaper.widget.theme.data.model.AppInfo
import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconItemModel
import thinklife.prism.wallpaper.widget.theme.data.model.theme.ThemeModel
import thinklife.prism.wallpaper.widget.theme.databinding.ActivityGetThemesBinding
import thinklife.prism.wallpaper.widget.theme.presentation.dialog.DownloadDialog
import thinklife.prism.wallpaper.widget.theme.presentation.main.MainActivity
import thinklife.prism.wallpaper.widget.theme.presentation.widget.adapter.WidgetSize
import thinklife.prism.wallpaper.widget.theme.presentation.widget.dialog.AddWidgetsBottomSheetDialog
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.adapter.SetIconAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.dialog.SelectAppBottomSheetDialog
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.model.IconPackItemUi
import thinklife.prism.wallpaper.widget.theme.presentation.iconpack.model.SetIconState
import thinklife.prism.wallpaper.widget.theme.presentation.theme.adapter.ThemePageAdapter
import thinklife.prism.wallpaper.widget.theme.presentation.theme.model.GetThemePage
import thinklife.prism.wallpaper.widget.theme.presentation.theme.viewmodel.GetThemeViewModel
import thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.dialog.SetWallpaperDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GetThemeActivity : BaseActivity<ActivityGetThemesBinding>(),
    SelectAppBottomSheetDialog.OnAppSelectedListener,
    SetWallpaperDialog.SetWallpaperListener {

    override val adsScreenId = AdsScreen.GET_THEME_VIEW

    override fun bindingView(): ActivityGetThemesBinding {
        return ActivityGetThemesBinding.inflate(layoutInflater)
    }

    companion object {
        private const val ARG_THEME = "arg_theme"

        fun newIntent(context: Context, theme: ThemeModel): Intent {
            return Intent(context, GetThemeActivity::class.java).apply {
                putExtra(ARG_THEME, theme)
            }
        }
    }

    private val viewModel by viewModels<GetThemeViewModel>()
    private val iconAdapter = SetIconAdapter()
    private val theme: ThemeModel? by lazy {
        intent.getParcel<ThemeModel>(ARG_THEME)
    }
    private var downloadDialog: DownloadDialog? = null
    private var availablePages = emptyList<GetThemePage>()
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            availablePages.getOrNull(position)?.let(viewModel::selectPage)
        }
    }

    @Inject
    lateinit var iconShortcutInstaller: IconShortcutInstaller

    override fun initConfig(savedInstanceState: Bundle?) {
        super.initConfig(savedInstanceState)
        val currentTheme = theme ?: run {
            finish()
            return
        }
        viewModel.load(currentTheme)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.rcvIcons.adapter = iconAdapter
        theme?.let { theme ->
            binding.ivWallpaper.loadWithLoading(
                theme.wallpaper.thumb.ifBlank { theme.wallpaper.url },
                binding.viewWallpaperLoading.root,
            )
        }
        val themeWidget = theme?.widget
        binding.tabWidgets.isVisible = themeWidget != null
        themeWidget?.let { widget ->
            binding.ivWidgetMedium.loadWithLoading(widget.contentM, binding.viewWidgetMediumLoading.root)
            binding.ivWidgetSmall.loadWithLoading(widget.contentS, binding.viewWidgetSmallLoading.root)
            binding.ivWidgetLarge.loadWithLoading(widget.contentL, binding.viewWidgetLargeLoading.root)
        }
        setupThemePager(hasWidget = themeWidget != null)
        renderPage(viewModel.selectedPage.value)
    }

    private fun setupThemePager(hasWidget: Boolean) {
        val pages = buildList {
            add(GetThemePage.WALLPAPERS to binding.pageWallpaper)
            add(GetThemePage.ICONS to binding.pageIcons)
            if (hasWidget) add(GetThemePage.WIDGETS to binding.pageWidgets)
        }
        availablePages = pages.map { it.first }
        pages.forEach { (_, view) ->
            (view.parent as? ViewGroup)?.removeView(view)
            view.isVisible = true
        }
        binding.themePager.apply {
            adapter = ThemePageAdapter(pages.map { it.second })
            offscreenPageLimit = pages.size
            registerOnPageChangeCallback(pageChangeCallback)
        }
    }

    override fun initListener() {
        super.initListener()

        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }

        binding.tabWallpapers.setOnClickListener {
            triggerAds(AdsTrigger.GET_THEME_CLICK_TAB_WALLPAPERS) {
                runWhenResumed {
                    viewModel.selectPage(GetThemePage.WALLPAPERS)
                }
            }
        }

        binding.tabIcons.setOnClickListener {
            triggerAds(AdsTrigger.GET_THEME_CLICK_TAB_ICONS) {
                runWhenResumed {
                    viewModel.selectPage(GetThemePage.ICONS)
                }
            }
        }

        binding.tabWidgets.setOnClickListener {
            triggerAds(AdsTrigger.GET_THEME_CLICK_TAB_WIDGETS) {
                runWhenResumed {
                    viewModel.selectPage(GetThemePage.WIDGETS)
                }
            }
        }

        binding.frameWidgetSmall.setOnClickListener {
            triggerAds(AdsTrigger.GET_THEME_CLICK_FRAME_WIDGET_SMALL) {
                runWhenResumed { openThemeWidgetDialog(WidgetSize.SMALL) }
            }
        }
        binding.frameWidgetMedium.setOnClickListener {
            triggerAds(AdsTrigger.GET_THEME_CLICK_FRAME_WIDGET_MEDIUM) {
                runWhenResumed { openThemeWidgetDialog(WidgetSize.MEDIUM) }
            }
        }
        binding.frameWidgetLarge.setOnClickListener {
            triggerAds(AdsTrigger.GET_THEME_CLICK_FRAME_WIDGET_LARGE) {
                runWhenResumed { openThemeWidgetDialog(WidgetSize.LARGE) }
            }
        }

        binding.btnApplyWallpaper.setOnClickListener {
            triggerAds(AdsTrigger.GET_THEME_CLICK_APPLY_WALLPAPER) {
                runWhenResumed {
                    if (viewModel.wallpaperDownloaded.value) {
                        SetWallpaperDialog.newInstance()
                            .show(supportFragmentManager, SetWallpaperDialog::class.java.simpleName)
                    } else {
                        startWallpaperDownload()
                    }
                }
            }
        }

        binding.btnInstallAll.setOnClickListener {
            triggerAds(AdsTrigger.GET_THEME_CLICK_INSTALL_ALL) {
                runWhenResumed {
                    val mappedItems = viewModel.mappedIconsPendingInstall()
                    if (mappedItems.isEmpty()) {
                        Toast.makeText(this, R.string.no_icons_to_install, Toast.LENGTH_SHORT).show()
                        return@runWhenResumed
                    }
                    installAllShortcuts(mappedItems)
                }
            }
        }

        binding.btnGoToHome.setOnClickListener {
            triggerAds(AdsTrigger.GET_THEME_CLICK_GO_TO_HOME) {
                runWhenResumed {
                    startActivity(MainActivity.newIntent(this).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                }
            }
        }

        iconAdapter.onActionClick = { item, state ->
            triggerAds(AdsTrigger.GET_THEME_CLICK_ICON_ADAPTER_ACTION_CLICK) {
                runWhenResumed {
                    viewModel.itemIconPackItemUi = item
                    when (state) {
                        SetIconState.APPLIED -> installShortcut(item)
                        SetIconState.INSTALL -> installShortcut(item)
                        SetIconState.CHOOSE -> showSelectApp(item)
                    }
                }
            }
        }

        iconAdapter.onEditClick = { item ->
            triggerAds(AdsTrigger.GET_THEME_CLICK_ICON_ADAPTER_EDIT_CLICK) {
                runWhenResumed {
                    showSelectApp(item)
                }
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.selectedPage.collectWithLifecycle(this) { page ->
            renderPage(page)
        }

        viewModel.wallpaperDownloaded.collectWithLifecycle(this) { downloaded ->
            if (!viewModel.wallpaperApplied.value) updateWallpaperAction(downloaded)
        }

        viewModel.wallpaperApplied.collectWithLifecycle(this) { applied ->
            if (applied) showAppliedState()
        }

        viewModel.wallpaperDownloadState.collectWithLifecycle(this) { state ->
            when (state) {
                is DownloadState.Downloading -> {
                    showDownloadDialog().updateProgress(state.percent)
                }

                DownloadState.Saving -> {
                    showDownloadDialog().updateProgress(100)
                }

                is DownloadState.Success -> {
                    dismissDownloadDialog()
                }

                is DownloadState.Failure -> {
                    dismissDownloadDialog()
                    Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }

        viewModel.applyWallpaperState.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Data -> showAppliedState()

                is UiState.Error -> {
                    binding.viewApplyLoading.root.isVisible = false
                    Toast.makeText(this, R.string.server_error_message, Toast.LENGTH_SHORT).show()
                }

                UiState.Loading -> {
                    binding.viewApplyLoading.root.isVisible = true
                }

                else -> Unit
            }
        }

        viewModel.iconsState.collectWithLifecycle(this) { state ->
            when (state) {
                is UiState.Data -> {
                    binding.viewIconsLoading.isVisible = false
                    binding.rcvIcons.isVisible = true
                    iconAdapter.submitList(state.value)
                }

                is UiState.Error -> {
                    binding.viewIconsLoading.isVisible = false
                    binding.rcvIcons.isVisible = true
                    iconAdapter.submitList(emptyList())
                }

                UiState.Loading -> {
                    binding.viewIconsLoading.isVisible = true
                    binding.rcvIcons.isVisible = false
                }

                else -> Unit
            }
        }
    }

    private fun renderPage(page: GetThemePage) {
        val selectedPage = page.takeIf(availablePages::contains) ?: GetThemePage.WALLPAPERS
        val pageIndex = availablePages.indexOf(selectedPage)
        if (pageIndex >= 0 && binding.themePager.currentItem != pageIndex) {
            binding.themePager.setCurrentItem(pageIndex, true)
        }

        binding.tabWallpapers.animateSegmentSelected(selectedPage == GetThemePage.WALLPAPERS)
        binding.tabIcons.animateSegmentSelected(selectedPage == GetThemePage.ICONS)
        binding.tabWidgets.animateSegmentSelected(selectedPage == GetThemePage.WIDGETS)
        binding.tabWallpapers.isSelected = selectedPage == GetThemePage.WALLPAPERS
        binding.tabIcons.isSelected = selectedPage == GetThemePage.ICONS
        binding.tabWidgets.isSelected = selectedPage == GetThemePage.WIDGETS
    }

    override fun onDestroy() {
        binding.themePager.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.themePager.adapter = null
        super.onDestroy()
    }

    private fun showAppliedState() {
        binding.viewApplyLoading.root.isVisible = false
        binding.viewApplied.isVisible = true
        binding.btnGoToHome.isInvisible = false
        binding.btnApplyWallpaper.isVisible = false
    }

    private fun updateWallpaperAction(downloaded: Boolean) {
        binding.btnApplyWallpaper.setIconRight(if (downloaded) 0 else R.drawable.ic_ad)
        if (downloaded) {
            binding.btnApplyWallpaper.setIcon(0)
            binding.btnApplyWallpaper.setText(getString(R.string.apply))
        } else {
            binding.btnApplyWallpaper.setIcon(R.drawable.ic_download)
            binding.btnApplyWallpaper.setText(getString(R.string.download))
        }
    }

    private fun startWallpaperDownload() {
        triggerRewardAds(AdsTrigger.GET_THEME_CLICK_START_WALLPAPER_DOWNLOAD) { performStartWallpaperDownload() }
    }

    private fun performStartWallpaperDownload() {
        showDownloadDialog().updateProgress(1)
        viewModel.downloadWallpaper()
    }

    private fun showDownloadDialog(): DownloadDialog {
        downloadDialog?.let { return it }
        (supportFragmentManager.findFragmentByTag(DownloadDialog.TAG) as? DownloadDialog)?.let {
            downloadDialog = it
            return it
        }
        return DownloadDialog.newInstance().also { dialog ->
            downloadDialog = dialog
            if (!supportFragmentManager.isStateSaved) {
                dialog.show(supportFragmentManager, DownloadDialog.TAG)
            }
        }
    }

    private fun dismissDownloadDialog() {
        val dialog = downloadDialog
        downloadDialog = null
        if (dialog?.isAdded == true) {
            dialog.dismissAllowingStateLoss()
        }
    }

    private fun showSelectApp(item: IconPackItemUi) {
        viewModel.setPendingIcon(item)
        SelectAppBottomSheetDialog.newInstance()
            .show(supportFragmentManager, SelectAppBottomSheetDialog::class.java.simpleName)
    }

    private fun installShortcut(item: IconPackItemUi) {
        if (!item.canInstall) return
        triggerRewardAds(AdsTrigger.GET_THEME_CLICK_INSTALL_SHORTCUT) { performInstallShortcut(item) }
    }

    private fun performInstallShortcut(item: IconPackItemUi) {
        if (!item.canInstall) return
        lifecycleScope.launch {
            val success = requestInstallShortcut(item)
            if (success) {
                viewModel.markIconApplied(item.id)
                val remainingIcons = viewModel.currentIconItems()
                    .filter { it.id != item.id }
                    .map { it.toIconItemModel() }
                startActivity(
                    AppliedThemeActivity.singleIconIntent(
                        this@GetThemeActivity,
                        remainingIcons
                    )
                )
            } else {
                Toast.makeText(
                    this@GetThemeActivity,
                    R.string.shortcut_install_failed,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun installAllShortcuts(items: List<IconPackItemUi>) {
        triggerRewardAds(AdsTrigger.GET_THEME_CLICK_INSTALL_ALL_SHORTCUTS) { performInstallAllShortcuts(items) }
    }

    private fun performInstallAllShortcuts(items: List<IconPackItemUi>) {
        lifecycleScope.launch {
            val appliedIds = items.mapNotNull { item ->
                if (requestInstallShortcut(item)) item.id else null
            }
            if (appliedIds.isNotEmpty()) {
                viewModel.markIconsApplied(appliedIds)
                startActivity(AppliedThemeActivity.allIconsIntent(this@GetThemeActivity))
            } else {
                Toast.makeText(
                    this@GetThemeActivity,
                    R.string.shortcut_install_failed,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private suspend fun requestInstallShortcut(item: IconPackItemUi): Boolean {
        val packageName = item.targetPackageName ?: return false
        return iconShortcutInstaller.installShortcut(
            targetPackageName = packageName,
            customIconUrl = item.iconUrl,
            label = item.targetLabel,
        ).isSuccess
    }

    private fun IconPackItemUi.toIconItemModel(): IconItemModel {
        return IconItemModel(
            packageName = targetPackageName.orEmpty(),
            appName = targetLabel,
            iconUrl = iconUrl,
        )
    }

    private fun openThemeWidgetDialog(size: WidgetSize) {
        val widgetModel = theme?.toWidgetsModel() ?: return
        AddWidgetsBottomSheetDialog.newInstance(
            widget = widgetModel,
            initialSize = size,
            isThemeWidget = true,
        ).show(supportFragmentManager, AddWidgetsBottomSheetDialog.TAG)
    }


    override fun onAppSelected(appInfo: AppInfo) {
        viewModel.applySelectedApp(appInfo)
    }

    override fun onSetAsWallpaper(wallpaperTarget: WallpaperTarget) {
        viewModel.applyWallpaper(wallpaperTarget)
    }

    override fun onHandleBackPressed() {
        triggerBackAds(AdsTrigger.GET_THEME_CLICK_BACK) { finish() }
    }

}
