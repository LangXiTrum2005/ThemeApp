package com.wavez.controllerwidget.ui.iconpack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.data.model.IconPackModel
import com.wavez.controllerwidget.data.repository.IconPackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IconPackListViewModel @Inject constructor(
    private val repo: IconPackRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<IconPackModel>>>(UiState.Idle)
    val state: StateFlow<UiState<List<IconPackModel>>> = _state.asStateFlow()

    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            repo.getIconPacks(forceRefresh)
                .onSuccess { _state.value = UiState.Data(it) }
                .onFailure {
                    _state.value = UiState.Error(it, it.message.orEmpty().ifBlank { "Unknown error" })
                }
        }
    }
}
