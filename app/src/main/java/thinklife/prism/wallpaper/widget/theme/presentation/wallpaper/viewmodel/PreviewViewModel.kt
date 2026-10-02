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

package thinklife.prism.wallpaper.widget.theme.presentation.wallpaper.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import thinklife.prism.wallpaper.widget.theme.core.download.DownloadState
import thinklife.prism.wallpaper.widget.theme.core.wallpaper.model.WallpaperTarget
import thinklife.prism.wallpaper.widget.theme.data.model.wallpaper.WallpaperModel
import thinklife.prism.wallpaper.widget.theme.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
) : ViewModel() {

    private val _downloadState = MutableSharedFlow<DownloadState<File>>(
        replay = 1,
        extraBufferCapacity = 64,
    )
    val downloadState: SharedFlow<DownloadState<File>> = _downloadState.asSharedFlow()

    var currentDownloadState: DownloadState<File> = DownloadState.Idle
        private set

    var wallpaperModel: WallpaperModel? = null
        private set

    fun setWallpaperModel(wallpaperModel: WallpaperModel?) {
        if (this.wallpaperModel != null || wallpaperModel == null) return
        this.wallpaperModel = wallpaperModel
    }

    fun downloadWallpaper() {
        if (currentDownloadState is DownloadState.Downloading ||
            currentDownloadState is DownloadState.Saving
        ) {
            return
        }
        wallpaperModel?.let {
            viewModelScope.launch {
                wallpaperRepository.downloadWallpaper(it)
                    .collect { downloadState ->
                        Log.d("Log_download", "downloadWallpaper: $downloadState")
                        currentDownloadState = downloadState
                        _downloadState.emit(downloadState)
                    }
            }
        }

    }

    fun isWallpaperDownloaded(wallpaperId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = wallpaperRepository.checkWallpaperDownloaded(wallpaperId)
            onResult(result)
        }
    }

    fun applyWallpaper(
        wallpaperModel: WallpaperModel,
        target: WallpaperTarget,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = wallpaperRepository.applyWallpaper(wallpaperModel, target)
            onResult(result)
        }
    }
}
