package com.wavez.controllerwidget.ui.home.fragment.mygallery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.data.model.ControllerModel
import com.wavez.controllerwidget.data.model.theme.ThemeModel
import com.wavez.controllerwidget.data.model.wallpaper.WallpaperModel
import com.wavez.controllerwidget.data.repository.ThemeRepository
import com.wavez.controllerwidget.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyGalleryViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val wallpaperRepository: WallpaperRepository,
) : ViewModel() {

    private val _themesState = MutableStateFlow<UiState<List<ThemeModel>>>(UiState.Idle)
    val themesState: StateFlow<UiState<List<ThemeModel>>> = _themesState.asStateFlow()

    private val _wallpapersState = MutableStateFlow<UiState<List<WallpaperModel>>>(UiState.Idle)
    val wallpapersState: StateFlow<UiState<List<WallpaperModel>>> = _wallpapersState.asStateFlow()

    init {
        observeDownloadedThemes()
        observeDownloadedWallpapers()
    }

    private fun observeDownloadedThemes() {
        viewModelScope.launch {
            themeRepository.observeDownloadedThemes()
                .onStart { _themesState.value = UiState.Loading }
                .catch { throwable ->
                    _themesState.value = UiState.Error(
                        throwable = throwable,
                        message = throwable.message.orEmpty(),
                    )
                }
                .collect { themes ->
                    _themesState.value = UiState.Data(themes)
                }
        }
    }

    private fun observeDownloadedWallpapers() {
        viewModelScope.launch {
            wallpaperRepository.observeDownloadedWallpapers()
                .onStart { _wallpapersState.value = UiState.Loading }
                .catch { throwable ->
                    _wallpapersState.value = UiState.Error(
                        throwable = throwable,
                        message = throwable.message.orEmpty(),
                    )
                }
                .collect { wallpapers ->
                    _wallpapersState.value = UiState.Data(wallpapers)
                }
        }
    }

}
