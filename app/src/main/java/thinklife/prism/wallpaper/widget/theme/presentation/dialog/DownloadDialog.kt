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

package thinklife.prism.wallpaper.widget.theme.presentation.dialog

import android.os.Bundle
import android.view.View
import thinklife.prism.wallpaper.widget.theme.core.base.BaseDialogFragment
import thinklife.prism.wallpaper.widget.theme.databinding.DialogDownloadBinding

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
