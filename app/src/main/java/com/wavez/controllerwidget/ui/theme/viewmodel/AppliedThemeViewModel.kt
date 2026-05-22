package com.wavez.controllerwidget.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.data.model.theme.ThemeModel
import com.wavez.controllerwidget.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppliedThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val _suggestedThemes = MutableStateFlow<UiState<List<ThemeModel>>>(UiState.Idle)
    val suggestedThemes: StateFlow<UiState<List<ThemeModel>>> = _suggestedThemes.asStateFlow()

    fun loadSuggestedThemes() {
        viewModelScope.launch {
            _suggestedThemes.value = UiState.Loading
            themeRepository.getThemes()
                .onSuccess { themes -> _suggestedThemes.value = UiState.Data(themes) }
                .onFailure { throwable ->
                    _suggestedThemes.value = UiState.Error(
                        throwable = throwable,
                        message = throwable.message.orEmpty(),
                    )
                }
        }
    }
}
