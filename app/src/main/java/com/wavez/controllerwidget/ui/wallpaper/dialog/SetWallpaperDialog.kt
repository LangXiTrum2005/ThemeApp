package com.wavez.controllerwidget.ui.wallpaper.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

    private var listener: SetWallpaperListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? SetWallpaperListener ?: parentFragment as? SetWallpaperListener
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        super.initConfig(view, savedInstanceState)
        hideDialogSystemBars()
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

    override fun onStart() {
        super.onStart()
        hideDialogSystemBars()
    }

    private fun hideDialogSystemBars() {
        dialog?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.navigationBarColor = Color.WHITE
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    interface SetWallpaperListener {
        fun onSetAsWallpaper(wallpaperTarget: WallpaperTarget)
    }
}
