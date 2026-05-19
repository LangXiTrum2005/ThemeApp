package com.wavez.controllerwidget.ui.wallpaper.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperTarget
import com.wavez.controllerwidget.data.model.WallpaperModel
import com.wavez.controllerwidget.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository
) : ViewModel() {

    private val _downloadState = MutableSharedFlow<DownloadState<File>>(
        replay = 1,
        extraBufferCapacity = 64,
    )
    val downloadState: SharedFlow<DownloadState<File>> = _downloadState.asSharedFlow()

    var currentDownloadState: DownloadState<File> = DownloadState.Idle
        private set

    fun downloadWallpaper(wallpaperModel: WallpaperModel) {
        if (currentDownloadState is DownloadState.Downloading) {
            return
        }
        viewModelScope.launch {
            wallpaperRepository.downloadWallpaper(wallpaperModel)
                .collect { downloadState ->
                    Log.d("Log_download", "downloadWallpaper: $downloadState")
                    currentDownloadState = downloadState
                    _downloadState.emit(downloadState)
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