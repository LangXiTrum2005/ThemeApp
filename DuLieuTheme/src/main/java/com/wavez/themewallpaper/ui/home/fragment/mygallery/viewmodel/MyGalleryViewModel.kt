package com.wavez.themewallpaper.ui.home.fragment.mygallery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.theme.ThemeModel
import com.wavez.themewallpaper.data.model.wallpaper.WallpaperModel
import com.wavez.themewallpaper.data.model.widget.WidgetsModel
import com.wavez.themewallpaper.data.repository.ThemeRepository
import com.wavez.themewallpaper.data.repository.WallpaperRepository
import com.wavez.themewallpaper.data.repository.WidgetRepository
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
    private val widgetRepository: WidgetRepository,
) : ViewModel() {

    private val _themesState = MutableStateFlow<UiState<List<ThemeModel>>>(UiState.Idle)
    val themesState: StateFlow<UiState<List<ThemeModel>>> = _themesState.asStateFlow()

    private val _wallpapersState = MutableStateFlow<UiState<List<WallpaperModel>>>(UiState.Idle)
    val wallpapersState: StateFlow<UiState<List<WallpaperModel>>> = _wallpapersState.asStateFlow()

    private val _widgetsState = MutableStateFlow<UiState<List<WidgetsModel>>>(UiState.Idle)
    val widgetsState: StateFlow<UiState<List<WidgetsModel>>> = _widgetsState.asStateFlow()

    init {
        observeDownloadedThemes()
        observeDownloadedWallpapers()
        observeAddedWidgets()
    }

    private fun observeAddedWidgets() {
        viewModelScope.launch {
            widgetRepository.observeAddedWidgets()
                .onStart { _widgetsState.value = UiState.Loading }
                .catch { throwable ->
                    _widgetsState.value = UiState.Error(
                        throwable = throwable,
                        message = throwable.message.orEmpty(),
                    )
                }
                .collect { widgets ->
                    _widgetsState.value = UiState.Data(widgets)
                }
        }
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
