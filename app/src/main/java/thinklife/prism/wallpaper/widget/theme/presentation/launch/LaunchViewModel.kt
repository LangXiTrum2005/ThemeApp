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

package thinklife.prism.wallpaper.widget.theme.presentation.launch

import androidx.lifecycle.viewModelScope
import thinklife.prism.wallpaper.widget.theme.core.base.BaseViewModel
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import thinklife.prism.wallpaper.widget.theme.data.local.InstalledAppProvider
import thinklife.prism.wallpaper.widget.theme.data.repository.IconPackRepository
import thinklife.prism.wallpaper.widget.theme.data.repository.ThemeRepository
import thinklife.prism.wallpaper.widget.theme.data.repository.WallpaperRepository
import javax.inject.Inject

/**
 * TODO(project): thêm warm-up dữ liệu đầu app tại đây (gọi API cấu hình, nạp cache...) và bọc
 * bằng `launchSafely` + `withTimeoutOrNull` để không kéo dài splash quá lâu.
 */
@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val sharedPref: SharedPref,
    private val installedAppProvider: InstalledAppProvider,
    private val wallpaperRepository: WallpaperRepository,
    private val iconPackRepository: IconPackRepository,
    private val themeRepository: ThemeRepository,
) : BaseViewModel() {

    fun markOpened() {
        sharedPref.hasOpenedBefore = true
    }

    fun preloadData() {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                installedAppProvider.getInstalledApps()
            }

            launch {
                themeRepository.getThemeCategories()
            }

            launch {
                themeRepository.getThemes()
            }

            launch {
                wallpaperRepository.getCategoriesUnlimited()
            }
        }
    }
}
