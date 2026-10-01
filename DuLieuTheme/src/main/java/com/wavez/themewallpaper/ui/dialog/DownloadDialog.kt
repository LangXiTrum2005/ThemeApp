package com.wavez.themewallpaper.ui.dialog

import android.os.Bundle
import android.view.View
import com.wavez.themewallpaper.core.base.BaseDialogFragment
import com.wavez.themewallpaper.databinding.DialogDownloadBinding

class DownloadDialog : BaseDialogFragment<DialogDownloadBinding>() {
    override fun bindingView(): DialogDownloadBinding {
        return DialogDownloadBinding.inflate(layoutInflater)
    }

    override fun isCancelable(): Boolean {
        return false
    }

    companion object {
        const val TAG = "DownloadDialog_TAG"
        fun newInstance(): DownloadDialog {
            return DownloadDialog()
        }
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        binding.progressLimit.max = 100
    }

    fun updateProgress(progress: Int) {
        if (!isAdded) return
        val safeProgress = progress.coerceIn(0, 100)
        binding.progressLimit.progress = safeProgress
    }
}
