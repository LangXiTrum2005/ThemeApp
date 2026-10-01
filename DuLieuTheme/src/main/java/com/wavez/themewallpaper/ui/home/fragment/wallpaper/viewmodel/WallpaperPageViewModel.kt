package com.wavez.themewallpaper.ui.home.fragment.wallpaper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel
import com.wavez.themewallpaper.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperPageViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository
) : ViewModel() {
    private val _wallpapersState =
        MutableStateFlow<UiState<List<WallpaperModel>>>(UiState.Idle)
    val wallpapersState: StateFlow<UiState<List<WallpaperModel>>> =
        _wallpapersState.asStateFlow()

    fun getWallpapersByCategory(categoryId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _wallpapersState.value = UiState.Loading
            wallpaperRepository.getWallpapersByCategory(categoryId, forceRefresh)
                .onSuccess { wallpapers ->
                    _wallpapersState.value = UiState.Data(wallpapers)
                }.onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _wallpapersState.value = UiState.Error(error, message)
                }
        }
    }

}