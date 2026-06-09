package com.wavez.controllerwidget.ui.wallpaper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.ads.AdsId
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.core.extensions.getParcel
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.extensions.loadWithLoading
import com.wavez.controllerwidget.core.extensions.visible
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperTarget
import com.wavez.controllerwidget.data.model.wallpaper.WallpaperModel
import com.wavez.controllerwidget.databinding.ActivityPreviewWallpaperBinding
import com.wavez.controllerwidget.ui.dialog.DownloadDialog
import com.wavez.controllerwidget.ui.dialog.PremiumThemeDialog
import com.wavez.controllerwidget.ui.premium.PremiumActivity
import com.wavez.controllerwidget.ui.wallpaper.dialog.SetWallpaperDialog
import com.wavez.controllerwidget.ui.wallpaper.viewmodel.PreviewViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PreviewWallpaperActivity : BaseActivity<ActivityPreviewWallpaperBinding>(),
    SetWallpaperDialog.SetWallpaperListener, PremiumThemeDialog.PremiumThemeListener {
    override fun bindingView(): ActivityPreviewWallpaperBinding {
        return ActivityPreviewWallpaperBinding.inflate(layoutInflater)
    }

    companion object {
        const val ARG_WALLPAPER = "arg_wallpaper"

        const val TAG = "PreviewWallpaperActivity"

        fun newIntent(context: Context, wallpaperModel: WallpaperModel): Intent {
            return Intent(context, PreviewWallpaperActivity::class.java).apply {
                putExtra(ARG_WALLPAPER, wallpaperModel)
            }
        }
    }

    private val viewModel by viewModels<PreviewViewModel>()

    private var downloadDialog: DownloadDialog? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        viewModel.setWallpaperModel(intent.getParcel(ARG_WALLPAPER) as? WallpaperModel)
        viewModel.wallpaperModel?.let { wallpaper ->
            binding.viewLoadingImage.visible()
            binding.ivImage.loadWithLoading(
                wallpaper.url, loadingView = binding.viewLoadingImage, errorView = binding.viewError
            )
            binding.viewPreview.ivImage.loadWithLoading(
                wallpaper.url,
                loadingView = binding.viewPreview.viewLoadingImage,
                errorView = binding.viewPreview.viewError
            )
            updateDownloadAction()
            updateUiWallpaperDownloaded(wallpaper)
        }
        loadBanner()
    }

    private fun loadBanner() {
        binding.bannerAds.registerBanner(
            this, adId = AdsId.getBannerInAppAds(),
            shouldLoad = sharedPref.shouldShowBannerInApp ,
        )
    }


    override fun onVipChangedState(isPremium: Boolean) {
        super.onVipChangedState(isPremium)
        updateDownloadAction()
    }

    override fun initListener() {
        super.initListener()

        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }

        binding.viewPreview.btnBack.setOnClickListener {
            onHandleBackPressed()
        }

        binding.btnDownload.setOnClickListener {
            handleDownloadClick()
        }

        binding.btnPreview.setOnClickListener {
            binding.viewPreview.root.visible()
        }

        binding.btnApply.setOnClickListener {
            SetWallpaperDialog.newInstance()
                .show(supportFragmentManager, SetWallpaperDialog::class.java.simpleName)
        }

    }

    override fun initObserver() {
        super.initObserver()

        lifecycleScope.launch {
            viewModel.downloadState.collect { state ->
                when (state) {
                    is DownloadState.Downloading -> {
                        showDownloadDialog().updateProgress(state.percent)
                    }

                    is DownloadState.Success -> {
                        dismissDownloadDialog()
                        updateUiWallpaperDownloaded(viewModel.wallpaperModel ?: return@collect)
                    }

                    is DownloadState.Failure -> {
                        dismissDownloadDialog()
                        Toast.makeText(
                            this@PreviewWallpaperActivity,
                            getString(R.string.download_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun updateUiWallpaperDownloaded(wallpaperModel: WallpaperModel) {
        viewModel.isWallpaperDownloaded(wallpaperModel.id, onResult = {
            binding.viewApply.isVisible = it
            binding.btnDownload.isVisible = !it
        })
    }

    private fun updateDownloadAction() {
        val canDownload = viewModel.canDownloadCurrentWallpaper()
        binding.btnDownload.setText(getString(if (canDownload) R.string.download else R.string.unlock))
        binding.btnDownload.setIcon(if (canDownload) R.drawable.ic_download else R.drawable.ic_lock_1)
    }

    private fun handleDownloadClick() {
        if (viewModel.canDownloadCurrentWallpaper()) {
            executeWithAds {
                startDownload()
            }
        } else {
            showPremiumDialog()
        }
    }

    private fun startDownload() {
        showDownloadDialog().updateProgress(1)
        viewModel.downloadWallpaper()
    }

    private fun showPremiumDialog() {
        if (supportFragmentManager.findFragmentByTag(PremiumThemeDialog.TAG) != null) return
        PremiumThemeDialog.newInstance().show(supportFragmentManager, PremiumThemeDialog.TAG)
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

    override fun onHandleBackPressed() {
        if (binding.viewPreview.root.isVisible) {
            binding.viewPreview.root.gone()
        } else {
            executeWithAds {
                super.onHandleBackPressed()
            }
        }
    }

    override fun onSetAsWallpaper(wallpaperTarget: WallpaperTarget) {
        viewModel.wallpaperModel?.let {
            binding.viewLoading.root.visible()
            viewModel.applyWallpaper(
                it, wallpaperTarget, onResult = { state ->
                    binding.viewLoading.root.gone()
                    if (state.isSuccess) {
                        startActivity(ApplyWallpaperActivity.newIntent(this, it))
                    }
                })
        }
    }

    override fun onWatchAdsComplete() {
        viewModel.unlockCurrentWallpaper()
        startDownload()
    }


    override fun onGoPremiumClick() {
        startActivity(PremiumActivity.newIntent(this))
    }


}
