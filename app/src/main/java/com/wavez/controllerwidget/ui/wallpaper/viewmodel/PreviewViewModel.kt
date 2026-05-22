package com.wavez.controllerwidget.ui.wallpaper.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperTarget
import com.wavez.controllerwidget.data.model.wallpaper.WallpaperModel
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

    fun canDownloadWallpaper(wallpaperModel: WallpaperModel): Boolean {
        return wallpaperRepository.canDownloadWallpaper(wallpaperModel)
    }

    fun canDownloadCurrentWallpaper(): Boolean {
        return wallpaperModel?.let(::canDownloadWallpaper) == true
    }

    fun unlockWallpaper(wallpaperModel: WallpaperModel) {
        wallpaperRepository.unlockWallpaper(wallpaperModel)
    }

    fun unlockCurrentWallpaper() {
        wallpaperModel?.let(::unlockWallpaper)
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
