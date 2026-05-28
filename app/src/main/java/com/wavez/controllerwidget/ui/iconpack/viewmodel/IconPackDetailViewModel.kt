package com.wavez.controllerwidget.ui.iconpack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.data.local.InstalledAppProvider
import com.wavez.controllerwidget.data.model.AppInfo
import com.wavez.controllerwidget.data.model.icon.IconItemModel
import com.wavez.controllerwidget.data.model.icon.IconPackModel
import com.wavez.controllerwidget.data.repository.IconPackRepository
import com.wavez.controllerwidget.data.repository.SharedPref
import com.wavez.controllerwidget.ui.iconpack.mapper.IconPackItemMapper
import com.wavez.controllerwidget.ui.iconpack.mapper.IconPackItemMapper.Companion.normalizedAppName
import com.wavez.controllerwidget.ui.iconpack.model.IconPackDetailState
import com.wavez.controllerwidget.ui.iconpack.model.IconPackItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IconPackDetailViewModel @Inject constructor(
    private val repo: IconPackRepository,
    private val installedAppProvider: InstalledAppProvider,
    private val sharedPref: SharedPref,
    private val iconPackItemMapper: IconPackItemMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<IconPackDetailState>>(UiState.Idle)
    val uiState: StateFlow<UiState<IconPackDetailState>> = _uiState.asStateFlow()

    private var pack: IconPackModel? = null
    private var installedAppsByPackage: Map<String, AppInfo> = emptyMap()
    private var installedAppsByName: Map<String, AppInfo> = emptyMap()

    // Keep manual mappings by stable icon id so icons with the same app name do not overwrite each other.
    private val manualMappings = mutableMapOf<Long, AppInfo>()
    private val appliedIconIds = mutableSetOf<Long>()
    private var pendingIconId: Long? = null
    private var installingAllAfterUnlock = false

    var currentIconPackId: String? = null
    var itemIconPackItemUi : IconPackItemUi? = null

    fun load(id: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.getIconPackDetail(id, forceRefresh)
                .onSuccess { loadedPack ->
                    Log.d("log_onSuccess", "onSuccess: ${loadedPack.icons}")

                    pack = loadedPack
                    // The pack already contains server icons; installed apps are loaded next to build UI state.
                    loadInstalledAppsAndRemap()
                }
                .onFailure { t ->
                    Log.d("log_error", "load: ${t.message}")
                    _uiState.value =
                        UiState.Error(t, t.message.orEmpty().ifBlank { "Unknown error" })
                }
        }
    }

    fun setPendingIcon(icon: IconPackItemUi) {
        pendingIconId = icon.id
    }

    fun applySelectedApp(app: AppInfo) {
        val iconId = pendingIconId ?: return
        manualMappings[iconId] = app
        pendingIconId = null

        // Do not reorder after user selection. Only the selected row should change.
        updateSingleIcon(iconId)
    }

    fun unlockIcon() {
        itemIconPackItemUi?.let {item->
            sharedPref.unlockIcon(item.id.toString())
            updateSingleIcon(item.id)
        }
    }

    fun unlockIcons(iconIds: Collection<Long>) {
        iconIds.forEach { iconId ->
            sharedPref.unlockIcon(iconId.toString())
        }
        refreshIconState()
    }

    fun setInstallingAllAfterUnlock(pending: Boolean) {
        installingAllAfterUnlock = pending
    }

    fun consumeInstallingAllAfterUnlock(): Boolean {
        return installingAllAfterUnlock.also {
            installingAllAfterUnlock = false
        }
    }

    fun refreshUnlockState() {
        refreshIconState()
    }

    private fun refreshIconState() {
        val currentState = uiState.value as? UiState.Data ?: return
        val iconsById = pack?.icons.orEmpty().associateBy(iconPackItemMapper::stableId)
        _uiState.value = UiState.Data(
            currentState.value.copy(
                items = currentState.value.items.map { item ->
                    iconsById[item.id]?.let(::mapIcon) ?: item
                }
            )
        )
    }

    fun markIconApplied(iconId: Long) {
        appliedIconIds.add(iconId)
        updateSingleIcon(iconId)
    }

    fun markIconsApplied(iconIds: List<Long>) {
        appliedIconIds.addAll(iconIds)
        val currentState = uiState.value as? UiState.Data ?: return
        _uiState.value = UiState.Data(
            currentState.value.copy(
                items = currentState.value.items.map { item ->
                    if (item.id in iconIds) item.copy(isApplied = true) else item
                }
            )
        )
    }

    fun mappedIconsPendingInstall(): List<IconPackItemUi> {
        val currentState = uiState.value as? UiState.Data ?: return emptyList()
        return currentState.value.items.filter { it.isInstallable && !it.isApplied }
    }

    private fun loadInstalledAppsAndRemap() {
        viewModelScope.launch {
            val apps = installedAppProvider.getInstalledApps()

            // Package lookup is exact. Name lookup is only a fallback for icons without package_name.
            installedAppsByPackage = apps.associateBy { it.packageName }
            installedAppsByName = apps.distinctBy { it.appName.normalizedAppName() }
                .associateBy { it.appName.normalizedAppName() }
            emitInitialMappedState()
        }
    }

    private fun emitInitialMappedState() {
        val currentPack = pack ?: return
        val sortedIcons = currentPack.icons.orEmpty()
            .mapIndexed { index, icon -> IndexedIcon(index, mapIcon(icon)) }
            // Initial ordering only. Later item changes must keep RecyclerView positions stable.
            .sortedWith(
                compareBy<IndexedIcon> { it.item.initialOrderGroup() }
                    .thenBy { it.index }
            )
            .map { it.item }

        _uiState.value = UiState.Data(
            IconPackDetailState(
                pack = currentPack,
                items = sortedIcons,
            )
        )
    }

    private fun updateSingleIcon(iconId: Long) {
        val currentState = uiState.value as? UiState.Data ?: return
        val currentPack = pack ?: return
        val sourceIcon = currentPack.icons.orEmpty()
            .firstOrNull { iconPackItemMapper.stableId(it) == iconId }
            ?: return

        val currentItems = currentState.value.items
        val targetIndex = currentItems.indexOfFirst { it.id == iconId }
        if (targetIndex == -1) return

        // Replace at the same index so DiffUtil can update via payload without scroll jump.
        val updatedItems = currentItems.mapIndexed { index, item ->
            if (index == targetIndex) mapIcon(sourceIcon) else item
        }

        _uiState.value = UiState.Data(
            currentState.value.copy(items = updatedItems)
        )
    }

    private fun mapIcon(icon: IconItemModel): IconPackItemUi {
        val iconId = iconPackItemMapper.stableId(icon)
        val item = iconPackItemMapper.map(
            icon = icon,
            installedAppsByPackage = installedAppsByPackage,
            installedAppsByName = installedAppsByName,
            manualMapping = manualMappings[iconId],
        )
        return item.copy(
            isApplied = appliedIconIds.contains(iconId)
        )
    }

    private fun IconPackItemUi.initialOrderGroup(): Int {
        return when {
            packageName != null && isInstalled -> ORDER_AUTO_MAPPED
            isManualMappingRequired -> ORDER_MANUAL_MAPPING
            else -> ORDER_UNMAPPED
        }
    }

    private data class IndexedIcon(
        val index: Int,
        val item: IconPackItemUi,
    )

    private companion object {
        const val ORDER_AUTO_MAPPED = 0
        const val ORDER_MANUAL_MAPPING = 1
        const val ORDER_UNMAPPED = 2
    }
}
