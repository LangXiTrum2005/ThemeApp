package com.wavez.themewallpaper.ui.iconpack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.icon.IconPackModel
import com.wavez.themewallpaper.data.repository.IconPackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplyIconsViewModel @Inject constructor(
    private val iconPackRepository: IconPackRepository
) : ViewModel() {

    private val _iconsState =
        MutableStateFlow<UiState<List<IconPackModel>>>(UiState.Idle)
    val iconsState: StateFlow<UiState<List<IconPackModel>>> =
        _iconsState.asStateFlow()

    var currentIconsId: String? = null

    fun getIcons() {
        viewModelScope.launch(Dispatchers.IO) {
            _iconsState.value = UiState.Loading
            iconPackRepository.getIconPacks()
                .onSuccess { wallpapers ->
                    val filtered = wallpapers.filter {
                        it.id != currentIconsId
                    }.take(10)

                    _iconsState.value = UiState.Data(filtered)
                }.onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _iconsState.value = UiState.Error(error, message)
                }
        }
    }

}