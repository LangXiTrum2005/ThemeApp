package com.wavez.themewallpaper.ui.home.fragment.widget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.widget.WidgetsModel
import com.wavez.themewallpaper.data.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetPageViewModel @Inject constructor(
    private val widgetRepository: WidgetRepository,
) : ViewModel() {

    private val _widgetsState = MutableStateFlow<UiState<List<WidgetsModel>>>(UiState.Idle)
    val widgetsState: StateFlow<UiState<List<WidgetsModel>>> = _widgetsState.asStateFlow()

    fun getWidgetsByCategory(categoryId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _widgetsState.value = UiState.Loading
            widgetRepository.getWidgetsByCategory(categoryId, forceRefresh)
                .onSuccess { widgets ->
                    _widgetsState.value = UiState.Data(widgets)
                }
                .onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _widgetsState.value = UiState.Error(error, message)
                }
        }
    }
}
