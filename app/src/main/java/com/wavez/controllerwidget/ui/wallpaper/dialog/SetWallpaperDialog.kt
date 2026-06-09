package com.wavez.controllerwidget.ui.wallpaper.dialog

import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.wavez.controllerwidget.core.base.BaseBottomSheetDialog
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperTarget
import com.wavez.controllerwidget.databinding.BottomDialogSetWallpaperBinding

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
