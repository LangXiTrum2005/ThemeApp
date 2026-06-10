package com.wavez.themewallpaper.ui.home.fragment.icons.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.icon.IconPackModel
import com.wavez.themewallpaper.data.repository.IconPackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IconsViewModel @Inject constructor(
    private val iconPackRepository: IconPackRepository
) : ViewModel() {

    private val _iconsResult =
        MutableStateFlow<UiState<List<IconPackModel>>>(UiState.Idle)
    val iconResult: StateFlow<UiState<List<IconPackModel>>> = _iconsResult

    fun getIcons() {
        if (_iconsResult.value is UiState.Loading) return
        viewModelScope.launch {
            _iconsResult.value = UiState.Loading
            val icons = iconPackRepository.getIconPacks()
            icons.onSuccess {
                _iconsResult.value = UiState.Data(it)
            }.onFailure {
                val message = it.message.orEmpty().ifBlank { "Unknown error" }
                _iconsResult.value = UiState.Error(it, message)
            }
        }
    }

}