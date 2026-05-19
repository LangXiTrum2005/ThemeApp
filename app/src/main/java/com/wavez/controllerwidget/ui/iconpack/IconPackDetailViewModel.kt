package com.wavez.controllerwidget.ui.iconpack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.data.local.iconpack.ManualIconMappingStore
import com.wavez.controllerwidget.data.model.IconMappingItem
import com.wavez.controllerwidget.data.model.IconPackModel
import com.wavez.controllerwidget.data.model.InstalledApp
import com.wavez.controllerwidget.data.repository.IconPackRepository
import com.wavez.controllerwidget.core.icons.BuildIconMappingUseCase
import com.wavez.controllerwidget.core.icons.CreateIconShortcutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class IconPackDetailUiState(
    val detail: UiState<IconPackModel> = UiState.Idle,
    val download: DownloadState<File> = DownloadState.Idle,
    val mapping: UiState<List<IconMappingItem>> = UiState.Idle,
)

@HiltViewModel
class IconPackDetailViewModel @Inject constructor(
    private val repo: IconPackRepository,
    private val buildIconMappingUseCase: BuildIconMappingUseCase,
    private val manualIconMappingStore: ManualIconMappingStore,
    private val createIconShortcutUseCase: CreateIconShortcutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(IconPackDetailUiState())
    val uiState: StateFlow<IconPackDetailUiState> = _uiState.asStateFlow()

    // Convenience accessors kept for backwards-compat with any existing UI code
    val detailState: StateFlow<UiState<IconPackModel>> get() = MutableStateFlow(_uiState.value.detail)
    val downloadState: StateFlow<DownloadState<File>> get() = MutableStateFlow(_uiState.value.download)
    val mappingState: StateFlow<UiState<List<IconMappingItem>>> get() = MutableStateFlow(_uiState.value.mapping)

    fun load(id: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(detail = UiState.Loading)
            repo.getIconPackDetail(id, forceRefresh)
                .onSuccess { model ->
                    _uiState.value = _uiState.value.copy(detail = UiState.Data(model))
                    rebuildMappingItems(id)
                }
                .onFailure { t ->
                    _uiState.value = _uiState.value.copy(
                        detail = UiState.Error(t, t.message.orEmpty().ifBlank { "Unknown error" })
                    )
                }
        }
    }

    fun downloadIfNeeded(iconPack: IconPackModel) {
        viewModelScope.launch {
            repo.downloadIconPack(iconPack).collect { state ->
                _uiState.value = _uiState.value.copy(download = state)
                if (state is DownloadState.Success) {
                    rebuildMappingItems(iconPack.id)
                }
            }
        }
    }

    fun selectAppForIcon(iconPackId: String, iconId: String, app: InstalledApp) {
        viewModelScope.launch {
            manualIconMappingStore.saveMapping(iconPackId, iconId, app.packageName, app.appName)
            rebuildMappingItems(iconPackId)
        }
    }

    fun removeManualMapping(iconPackId: String, iconId: String) {
        viewModelScope.launch {
            manualIconMappingStore.removeMapping(iconPackId, iconId)
            rebuildMappingItems(iconPackId)
        }
    }

    fun createShortcut(item: IconMappingItem): Boolean {
        return runCatching { createIconShortcutUseCase(item) }.getOrDefault(false)
    }

    fun rebuildMappingItems(iconPackId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(mapping = UiState.Loading)
            val local = repo.getLocalIconPack(iconPackId)
            if (local == null) {
                _uiState.value = _uiState.value.copy(
                    mapping = UiState.Error(
                        IllegalStateException("Icon pack not installed: $iconPackId"),
                        "Icon pack not installed",
                    )
                )
                return@launch
            }
            val manualMappings = manualIconMappingStore.getMappings(iconPackId)
            runCatching { buildIconMappingUseCase(local, manualMappings) }
                .onSuccess { items ->
                    _uiState.value = _uiState.value.copy(mapping = UiState.Data(items))
                }
                .onFailure { t ->
                    _uiState.value = _uiState.value.copy(
                        mapping = UiState.Error(t, t.message.orEmpty().ifBlank { "Unknown error" })
                    )
                }
        }
    }
}
