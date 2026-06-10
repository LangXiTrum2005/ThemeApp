package com.wavez.themewallpaper.ui.wallpaper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel
import com.wavez.themewallpaper.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplyWallpaperViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository
) : ViewModel() {

    private val _wallpapersState =
        MutableStateFlow<UiState<List<WallpaperModel>>>(UiState.Idle)
    val wallpapersState: StateFlow<UiState<List<WallpaperModel>>> =
        _wallpapersState.asStateFlow()

    var currentWallpaper: WallpaperModel? = null

    fun getWallpapersByCategory(categoryId: String = currentWallpaper?.categoryId.orEmpty()) {
        viewModelScope.launch(Dispatchers.IO) {
            _wallpapersState.value = UiState.Loading
            wallpaperRepository.getWallpapersByCategory(categoryId)
                .onSuccess { wallpapers ->

                    val filtered = wallpapers.filter {
                        it.id != currentWallpaper?.id
                    }.take(10)

                    _wallpapersState.value = UiState.Data(filtered)
                }.onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _wallpapersState.value = UiState.Error(error, message)
                }
        }
    }

}