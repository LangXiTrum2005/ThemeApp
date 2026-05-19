package com.wavez.controllerwidget.ui.wallpaper

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.wavez.controllerwidget.core.base.BaseActivity
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.core.extensions.getParcel
import com.wavez.controllerwidget.core.extensions.gone
import com.wavez.controllerwidget.core.extensions.visible
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperTarget
import com.wavez.controllerwidget.data.model.WallpaperModel
import com.wavez.controllerwidget.databinding.ActivityPreviewWallpaperBinding
import com.wavez.controllerwidget.ui.dialog.DownloadDialog
import com.wavez.controllerwidget.ui.wallpaper.dialog.SetWallpaperDialog
import com.wavez.controllerwidget.ui.wallpaper.viewmodel.PreviewViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PreviewWallpaperActivity : BaseActivity<ActivityPreviewWallpaperBinding>(),
    SetWallpaperDialog.SetWallpaperListener {
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

    private val wallpaperModel: WallpaperModel? by lazy {
        intent.getParcel(ARG_WALLPAPER) as? WallpaperModel
    }

    private var downloadDialog: DownloadDialog? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        wallpaperModel?.let { wallpaper ->
            binding.viewLoadingImage.visible()
            Glide.with(this)
                .load(wallpaper.url)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        p0: GlideException?,
                        p1: Any?,
                        p2: Target<Drawable?>?,
                        p3: Boolean
                    ): Boolean {
                        binding.viewLoadingImage.gone()
                        return false
                    }

                    override fun onResourceReady(
                        p0: Drawable?,
                        p1: Any?,
                        p2: Target<Drawable?>?,
                        p3: DataSource?,
                        p4: Boolean
                    ): Boolean {
                        binding.viewLoadingImage.gone()
                        return false
                    }
                })
                .into(binding.ivImage)

            updateUiWallpaperDownloaded(wallpaper)
        }
    }

    override fun initListener() {
        super.initListener()

        binding.btnBack.setOnClickListener {
            onHandleBackPressed()
        }

        binding.btnDownload.setOnClickListener {
            val model = wallpaperModel ?: return@setOnClickListener
            showDownloadDialog().updateProgress(1)
            viewModel.downloadWallpaper(model)
        }

        binding.btnPreview.setOnClickListener {
            binding.viewApply.gone()
            binding.tvPreview.visible()
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
                        Log.d(TAG, "initObserver: Downloading ${state.percent}")
                        showDownloadDialog().updateProgress(state.percent)
                    }

                    is DownloadState.Success -> {
                        dismissDownloadDialog()
                        updateUiWallpaperDownloaded(wallpaperModel ?: return@collect)
                        Toast.makeText(
                            this@PreviewWallpaperActivity,
                            "Downloaded successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is DownloadState.Failure -> {
                        dismissDownloadDialog()
                        Toast.makeText(
                            this@PreviewWallpaperActivity,
                            "Download failed",
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
        if (binding.tvPreview.isVisible) {
            binding.viewApply.visible()
            binding.tvPreview.gone()
        } else {
            super.onHandleBackPressed()
        }
    }

    override fun onSetAsWallpaper(wallpaperTarget: WallpaperTarget) {
        wallpaperModel?.let {
            binding.viewLoading.visible()
            viewModel.applyWallpaper(
                it,
                wallpaperTarget,
                onResult = { state ->
                    binding.viewLoading.gone()
                    if (state.isSuccess) {
                        startActivity(ApplyWallpaperActivity.newIntent(this, it))
                    }
                }
            )
        }
    }
}
