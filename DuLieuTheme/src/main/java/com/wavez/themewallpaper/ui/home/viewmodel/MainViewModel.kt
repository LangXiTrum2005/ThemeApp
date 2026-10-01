package com.wavez.themewallpaper.ui.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.core.wallpaper.model.WallpaperTarget
import com.wavez.themewallpaper.data.local.InstalledAppProvider
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel
import com.wavez.themewallpaper.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val provider: InstalledAppProvider
) : ViewModel() {

    private val _wallpapersState =
        MutableStateFlow<UiState<List<WallpaperModel>>>(UiState.Idle)
    val wallpapersState: StateFlow<UiState<List<WallpaperModel>>> =
        _wallpapersState.asStateFlow()

    private val _applyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val applyState: StateFlow<UiState<Unit>> = _applyState.asStateFlow()

    fun getWallpapers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _wallpapersState.value = UiState.Loading
            Log.d("Log_data", "getWallpapers: start")

            val result = wallpaperRepository.getAllWallpapersUnlimited(forceRefresh)
            result.onSuccess { wallpapers ->
                Log.d("Log_data", "getWallpapers: $wallpapers")
                _wallpapersState.value = UiState.Data(wallpapers)
            }.onFailure { error ->
                val message = error.message.orEmpty().ifBlank { "Unknown error" }
                Log.e("Log_data", "onFailure: $message", error)
                _wallpapersState.value = UiState.Error(error, message)
            }
        }
    }

    fun applyWallpaper(wallpaper: WallpaperModel) {
        viewModelScope.launch {
            _applyState.value = UiState.Loading
            wallpaperRepository.applyWallpaper(wallpaper, WallpaperTarget.HOME)
                .onSuccess { _applyState.value = UiState.Data(Unit) }
                .onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    Log.e("Log_data", "applyWallpaper failed: $message", error)
                    _applyState.value = UiState.Error(error, message)
                }
        }
    }
}