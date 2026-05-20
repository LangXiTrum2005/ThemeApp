package com.wavez.controllerwidget.ui.iconpack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.data.local.InstalledAppProvider
import com.wavez.controllerwidget.data.model.AppInfo
import com.wavez.controllerwidget.data.model.IconItemModel
import com.wavez.controllerwidget.data.model.IconPackModel
import com.wavez.controllerwidget.data.repository.IconPackRepository
import com.wavez.controllerwidget.data.repository.SharedPref
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
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<IconPackDetailState>>(UiState.Idle)
    val uiState: StateFlow<UiState<IconPackDetailState>> = _uiState.asStateFlow()

    private var pack: IconPackModel? = null
    private var installedAppsByPackage: Map<String, AppInfo> = emptyMap()
    private var installedAppsByName: Map<String, AppInfo> = emptyMap()

    // Keep manual mappings by stable icon id so icons with the same app name do not overwrite each other.
    private val manualMappings = mutableMapOf<Long, AppInfo>()
    private var pendingIconId: Long? = null

    fun load(id: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.getIconPackDetail(id, forceRefresh)
                .onSuccess { loadedPack ->
                    pack = loadedPack
                    // The pack already contains server icons; installed apps are loaded next to build UI state.
                    loadInstalledAppsAndRemap()
                }
                .onFailure { t ->
                    Log.d("log_error", "load: ${t.message}")
                    _uiState.value = UiState.Error(t, t.message.orEmpty().ifBlank { "Unknown error" })
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

    fun unlockIcon(item: IconPackItemUi) {
        sharedPref.unlockIcon(item.id.toString())
        updateSingleIcon(item.id)
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
        val sortedIcons = currentPack.previewIcons.orEmpty()
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
        val sourceIcon = currentPack.previewIcons.orEmpty()
            .firstOrNull { it.stableId() == iconId }
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
        val declaredPackageName = icon.packageName.takeIf { it.isNotBlank() }
        val manualApp = manualMappings[icon.stableId()]
        val installedApp = manualApp ?: findInstalledApp(icon, declaredPackageName)
        val isManual = declaredPackageName == null

        return IconPackItemUi(
            id = icon.stableId(),
            packageName = declaredPackageName,
            appName = icon.appName,
            iconUrl = icon.iconUrl,
            installedApp = installedApp,
            isInstalled = declaredPackageName != null && installedAppsByPackage.containsKey(
                declaredPackageName
            ),
            isMapped = installedApp != null,
            selectedPackageName = manualApp?.packageName,
            selectedAppName = manualApp?.appName,
            selectedAppIcon = manualApp?.icon,
            isManualMappingRequired = isManual,
            isUnlocked = sharedPref.isVip || sharedPref.isIconUnlocked(icon.stableId().toString()),
        )
    }

    private fun findInstalledApp(icon: IconItemModel, packageName: String?): AppInfo? {
        packageName?.let { installedAppsByPackage[it] }?.let { return it }
        if (packageName != null) return null

        // Server items without package_name may still match an installed launcher app by label.
        val normalizedIconName = icon.appName.normalizedAppName()
        return installedAppsByName[normalizedIconName]
    }

    private fun IconPackItemUi.initialOrderGroup(): Int {
        return when {
            packageName != null && isInstalled -> ORDER_AUTO_MAPPED
            isManualMappingRequired -> ORDER_MANUAL_MAPPING
            else -> ORDER_UNMAPPED
        }
    }

    private fun IconItemModel.stableId(): Long {
        return listOf(packageName, appName, iconUrl).joinToString("|").hashCode().toLong()
    }

    private fun String.normalizedAppName(): String {
        return lowercase().filter { it.isLetterOrDigit() }
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
