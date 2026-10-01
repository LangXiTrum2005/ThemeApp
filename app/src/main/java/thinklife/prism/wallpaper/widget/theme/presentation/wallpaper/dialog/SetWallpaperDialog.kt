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

package thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.dialog

import android.content.Context
import thinklife.prism.wallpaper.widget.theme.core.base.BaseBottomSheetDialog
import thinklife.prism.wallpaper.widget.theme.core.wallpaper.model.WallpaperTarget
import thinklife.prism.wallpaper.widget.theme.databinding.BottomDialogSetWallpaperBinding

class SetWallpaperDialog : BaseBottomSheetDialog<BottomDialogSetWallpaperBinding>() {
    override fun bindingView(): BottomDialogSetWallpaperBinding {
        return BottomDialogSetWallpaperBinding.inflate(layoutInflater)
    }

    companion object {
        fun newInstance(): SetWallpaperDialog {
            return SetWallpaperDialog()
        }
    }

    override val hideNavigationBar: Boolean
        get() = true

    private var listener: SetWallpaperListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? SetWallpaperListener ?: parentFragment as? SetWallpaperListener
    }

    override fun initListener() {
        super.initListener()

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnHomeScreen.setOnClickListener {
            listener?.onSetAsWallpaper(WallpaperTarget.HOME)
            dismiss()
        }

        binding.btnLockScreen.setOnClickListener {
            listener?.onSetAsWallpaper(WallpaperTarget.LOCK)
            dismiss()
        }

        binding.btnBoth.setOnClickListener {
            listener?.onSetAsWallpaper(WallpaperTarget.BOTH)
            dismiss()
        }
    }

    interface SetWallpaperListener {
        fun onSetAsWallpaper(wallpaperTarget: WallpaperTarget)
    }
}
