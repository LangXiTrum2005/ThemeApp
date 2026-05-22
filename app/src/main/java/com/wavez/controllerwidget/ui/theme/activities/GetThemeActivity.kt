package com.wavez.controllerwidget.ui.theme.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.core.extensions.animateSegmentSelected
import com.wavez.controllerwidget.core.extensions.collectWithLifecycle
import com.wavez.controllerwidget.core.extensions.getParcel
import com.wavez.controllerwidget.core.extensions.loadWithLoading
import com.wavez.controllerwidget.core.extensions.switchVisibleWithSlideFade
import com.wavez.controllerwidget.core.icons.IconShortcutInstaller
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperTarget
import com.wavez.controllerwidget.data.model.AppInfo
import com.wavez.controllerwidget.data.model.icon.IconItemModel
import com.wavez.controllerwidget.data.model.theme.ThemeModel
import com.wavez.controllerwidget.databinding.ActivityGetThemesBinding
import com.wavez.controllerwidget.ui.dialog.DownloadDialog
import com.wavez.controllerwidget.ui.iconpack.adapter.SetIconAdapter
import com.wavez.controllerwidget.ui.iconpack.dialog.SelectAppBottomSheetDialog
import com.wavez.controllerwidget.ui.iconpack.model.IconPackItemUi
import com.wavez.controllerwidget.ui.iconpack.model.SetIconState
import com.wavez.controllerwidget.ui.theme.model.GetThemePage
import com.wavez.controllerwidget.ui.theme.viewmodel.GetThemeViewModel
import com.wavez.controllerwidget.ui.wallpaper.dialog.SetWallpaperDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GetThemeActivity : BaseActivity<ActivityGetThemesBinding>(),
    SelectAppBottomSheetDialog.OnAppSelectedListener,
    SetWallpaperDialog.SetWallpaperListener {
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
    private var latestIconItems: List<IconPackItemUi> = emptyList()
    private var downloadDialog: DownloadDialog? = null

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
        renderPage(viewModel.selectedPage.value)
    }

    override fun initListener() {
        super.initListener()

        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }

        binding.tabWallpapers.setOnClickListener {
            viewModel.selectPage(GetThemePage.WALLPAPERS)
        }

        binding.tabIcons.setOnClickListener {
            viewModel.selectPage(GetThemePage.ICONS)
        }

        binding.btnApplyWallpaper.setOnClickListener {
            if (viewModel.wallpaperDownloaded.value) {
                SetWallpaperDialog.newInstance()
                    .show(supportFragmentManager, SetWallpaperDialog::class.java.simpleName)
            } else {
                showDownloadDialog().updateProgress(1)
                viewModel.downloadWallpaper()
            }
        }

        binding.btnInstallAll.setOnClickListener {
            val installableItems = latestIconItems.filter { it.canInstall }
            if (installableItems.isEmpty()) {
                Toast.makeText(this, R.string.no_icons_to_install, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            installAllShortcuts(installableItems)
        }

        iconAdapter.onActionClick = { item, state ->
            when (state) {
                SetIconState.APPLIED -> Unit
                SetIconState.UNLOCK -> viewModel.unlockIcon(item)
                SetIconState.INSTALL -> installShortcut(item)
                SetIconState.CHOOSE -> showSelectApp(item)
            }
        }

        iconAdapter.onEditClick = { item ->
            showSelectApp(item)
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.selectedPage.collectWithLifecycle(this) { page ->
            renderPage(page)
        }

        viewModel.wallpaperDownloaded.collectWithLifecycle(this) { downloaded ->
            updateWallpaperAction(downloaded)
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
                is UiState.Data -> {
                    binding.viewApplyLoading.root.isVisible = false
                    binding.viewApplied.isVisible = true
                    binding.btnApplyWallpaper.isVisible = false
                }

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
                    latestIconItems = state.value
                    iconAdapter.submitList(state.value)
                }

                is UiState.Error -> {
                    binding.viewIconsLoading.isVisible = false
                    binding.rcvIcons.isVisible = true
                    latestIconItems = emptyList()
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
        val isWallpaper = page == GetThemePage.WALLPAPERS
        switchVisibleWithSlideFade(
            showView = if (isWallpaper) binding.pageWallpaper else binding.pageIcons,
            hideView = if (isWallpaper) binding.pageIcons else binding.pageWallpaper,
            slideFromRight = isWallpaper,
        )
        binding.tabWallpapers.animateSegmentSelected(isWallpaper)
        binding.tabIcons.animateSegmentSelected(!isWallpaper)
        binding.tabWallpapers.isSelected = isWallpaper
        binding.tabIcons.isSelected = !isWallpaper
    }

    private fun updateWallpaperAction(downloaded: Boolean) {
        if (downloaded) {
            binding.btnApplyWallpaper.setIcon(0)
            binding.btnApplyWallpaper.setText(getString(R.string.apply))
        } else {
            binding.btnApplyWallpaper.setIcon(R.drawable.ic_download)
            binding.btnApplyWallpaper.setText(getString(R.string.download))
        }
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
        downloadDialog?.dismiss()
        downloadDialog = null
    }

    private fun showSelectApp(item: IconPackItemUi) {
        viewModel.setPendingIcon(item)
        SelectAppBottomSheetDialog.newInstance()
            .show(supportFragmentManager, SelectAppBottomSheetDialog::class.java.simpleName)
    }

    private fun installShortcut(item: IconPackItemUi) {
        if (!item.canInstall) return
        lifecycleScope.launch {
            val success = requestInstallShortcut(item)
            if (success) {
                viewModel.markIconApplied(item.id)
                val remainingIcons = latestIconItems
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

    override fun onAppSelected(appInfo: AppInfo) {
        viewModel.applySelectedApp(appInfo)
    }

    override fun onSetAsWallpaper(wallpaperTarget: WallpaperTarget) {
        viewModel.applyWallpaper(wallpaperTarget)
    }

}
