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

import thinklife.prism.wallpaper.widget.theme.ads.AdsTrigger
import thinklife.prism.wallpaper.widget.theme.ads.AdsScreen
import android.content.Context
import thinklife.prism.wallpaper.widget.theme.core.base.BaseBottomSheetDialog
import thinklife.prism.wallpaper.widget.theme.core.wallpaper.model.WallpaperTarget
import thinklife.prism.wallpaper.widget.theme.databinding.BottomDialogSetWallpaperBinding

class SetWallpaperDialog : BaseBottomSheetDialog<BottomDialogSetWallpaperBinding>() {

    override val adsScreenId = AdsScreen.SET_WALLPAPER_VIEW

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
            triggerAds(AdsTrigger.SET_WALLPAPER_CLICK_CLOSE) {
                runWhenResumed {
                    dismiss()
                }
            }
        }

        binding.btnHomeScreen.setOnClickListener {
            triggerRewardAds(AdsTrigger.SET_WALLPAPER_CLICK_HOME_SCREEN) {
                runWhenResumed {
                    listener?.onSetAsWallpaper(WallpaperTarget.HOME)
                    dismiss()
                }
            }
        }

        binding.btnLockScreen.setOnClickListener {
            triggerRewardAds(AdsTrigger.SET_WALLPAPER_CLICK_LOCK_SCREEN) {
                runWhenResumed {
                    listener?.onSetAsWallpaper(WallpaperTarget.LOCK)
                    dismiss()
                }
            }
        }

        binding.btnBoth.setOnClickListener {
            triggerRewardAds(AdsTrigger.SET_WALLPAPER_CLICK_BOTH) {
                runWhenResumed {
                    listener?.onSetAsWallpaper(WallpaperTarget.BOTH)
                    dismiss()
                }
            }
        }
    }

    interface SetWallpaperListener {
        fun onSetAsWallpaper(wallpaperTarget: WallpaperTarget)
    }
}
