package com.wavez.controllerwidget.ui.iconpack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.data.model.IconMappingItem
import com.wavez.controllerwidget.data.model.IconPackModel
import com.wavez.controllerwidget.data.model.ManualIconMapping
import com.wavez.controllerwidget.data.repository.IconPackRepository
import com.wavez.controllerwidget.domain.usecase.BuildIconMappingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class IconPackDetailViewModel @Inject constructor(
    private val repo: IconPackRepository,
    private val buildIconMappingUseCase: BuildIconMappingUseCase,
) : ViewModel() {

    private val _detailState = MutableStateFlow<UiState<IconPackModel>>(UiState.Idle)
    val detailState: StateFlow<UiState<IconPackModel>> = _detailState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState<File>>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState<File>> = _downloadState.asStateFlow()

    private val _mappingState = MutableStateFlow<UiState<List<IconMappingItem>>>(UiState.Idle)
    val mappingState: StateFlow<UiState<List<IconMappingItem>>> = _mappingState.asStateFlow()

    // Held in memory until a persistence layer is added (TODO: persist to Room or DataStore)
    private val manualMappings = mutableListOf<ManualIconMapping>()

    fun loadDetail(id: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            repo.getIconPackDetail(id, forceRefresh)
                .onSuccess { _detailState.value = UiState.Data(it) }
                .onFailure {
                    _detailState.value =
                        UiState.Error(it, it.message.orEmpty().ifBlank { "Unknown error" })
                }
        }
    }

    fun downloadIconPack(iconPack: IconPackModel) {
        viewModelScope.launch {
            repo.downloadIconPack(iconPack).collect { state ->
                _downloadState.value = state
                if (state is DownloadState.Success) {
                    buildMapping(iconPack.id)
                }
            }
        }
    }

    fun buildMapping(iconPackId: String) {
        viewModelScope.launch {
            _mappingState.value = UiState.Loading
            val local = repo.getLocalIconPack(iconPackId)
            if (local == null) {
                _mappingState.value = UiState.Error(
                    IllegalStateException("Icon pack not installed: $iconPackId"),
                    "Icon pack not installed",
                )
                return@launch
            }
            runCatching { buildIconMappingUseCase(local, manualMappings.toList()) }
                .onSuccess { _mappingState.value = UiState.Data(it) }
                .onFailure {
                    _mappingState.value =
                        UiState.Error(it, it.message.orEmpty().ifBlank { "Unknown error" })
                }
        }
    }

    /**
     * Adds or replaces a manual mapping for an icon, then rebuilds the mapping list.
     * TODO: persist manualMappings to Room/DataStore so they survive process death.
     */
    fun addManualMapping(mapping: ManualIconMapping) {
        manualMappings.removeAll { it.iconId == mapping.iconId }
        manualMappings.add(mapping)
        (detailState.value as? UiState.Data)?.value?.id?.let { buildMapping(it) }
    }
}
