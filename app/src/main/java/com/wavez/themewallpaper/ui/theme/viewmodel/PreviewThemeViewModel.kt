package com.wavez.themewallpaper.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.theme.ThemeModel
import com.wavez.themewallpaper.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviewThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val _saveEvent = MutableSharedFlow<UiState<Unit>>()
    val saveEvent: SharedFlow<UiState<Unit>> = _saveEvent.asSharedFlow()

    private val _themeState = MutableStateFlow<UiState<ThemeModel>>(UiState.Idle)
    val themeState: StateFlow<UiState<ThemeModel>> = _themeState.asStateFlow()

    var currentTheme: ThemeModel? = null

    fun setTheme(theme: ThemeModel?) {
        currentTheme = theme
        if (theme != null) {
            _themeState.value = UiState.Data(theme)
        }
    }

    fun loadTheme(themeId: String) {
        if (themeId.isBlank()) return
        viewModelScope.launch {
            _themeState.value = UiState.Loading
            themeRepository.getThemeById(themeId)
                .onSuccess { theme ->
                    currentTheme = theme
                    _themeState.value = UiState.Data(theme)
                }
                .onFailure { throwable ->
                    _themeState.value = UiState.Error(
                        throwable = throwable,
                        message = throwable.message.orEmpty().ifBlank { "Unknown error" },
                    )
                }
        }
    }

    fun continueToGetTheme() {
        viewModelScope.launch {
            currentTheme?.let { theme ->
                if (themeRepository.checkThemeSaved(theme.id)) {
                    _saveEvent.emit(UiState.Data(Unit))
                    return@launch
                }
                themeRepository.saveTheme(theme)
                    .onSuccess { _saveEvent.emit(UiState.Data(Unit)) }
                    .onFailure { throwable ->
                        _saveEvent.emit(
                            UiState.Error(
                                throwable = throwable,
                                message = throwable.message.orEmpty().ifBlank { "Unknown error" },
                            )
                        )
                    }
            }
        }
    }
}
