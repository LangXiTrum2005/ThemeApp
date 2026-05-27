package com.wavez.controllerwidget.ui.home.fragment.theme.viewmodel

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
class ThemePageViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val _themesState = MutableStateFlow<UiState<List<ThemeModel>>>(UiState.Idle)
    val themesState: StateFlow<UiState<List<ThemeModel>>> = _themesState.asStateFlow()

    fun getThemesByCategory(categoryId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _themesState.value = UiState.Loading
            themeRepository.getThemesByCategory(categoryId, forceRefresh)
                .onSuccess { themes ->
                    _themesState.value = UiState.Data(themes)
                }
                .onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _themesState.value = UiState.Error(error, message)
                }
        }
    }
}
