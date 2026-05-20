package com.wavez.controllerwidget.ui.iconpack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.data.local.InstalledAppProvider
import com.wavez.controllerwidget.data.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectAppViewModel @Inject constructor(
    private val provider: InstalledAppProvider,
) : ViewModel() {

    private val _apps = MutableStateFlow<UiState<List<AppInfo>>>(UiState.Idle)
    val apps: StateFlow<UiState<List<AppInfo>>> = _apps.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (_apps.value is UiState.Data) return
        viewModelScope.launch {
            _apps.value = UiState.Loading
            runCatching { provider.getInstalledApps() }
                .onSuccess { _apps.value = UiState.Data(it) }
                .onFailure { t ->
                    _apps.value = UiState.Error(t, t.message.orEmpty().ifBlank { "Failed to load apps" })
                }
        }
    }
}
