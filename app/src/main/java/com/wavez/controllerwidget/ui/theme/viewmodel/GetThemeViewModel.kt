package com.wavez.controllerwidget.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.core.wallpaper.model.WallpaperTarget
import com.wavez.controllerwidget.data.local.InstalledAppProvider
import com.wavez.controllerwidget.data.model.AppInfo
import com.wavez.controllerwidget.data.model.theme.ThemeModel
import com.wavez.controllerwidget.data.model.wallpaper.WallpaperModel
import com.wavez.controllerwidget.data.repository.SharedPref
import com.wavez.controllerwidget.data.repository.ThemeRepository
import com.wavez.controllerwidget.data.repository.WallpaperRepository
import com.wavez.controllerwidget.ui.iconpack.mapper.IconPackItemMapper
import com.wavez.controllerwidget.ui.iconpack.mapper.IconPackItemMapper.Companion.normalizedAppName
import com.wavez.controllerwidget.ui.iconpack.model.IconPackItemUi
import com.wavez.controllerwidget.ui.theme.model.GetThemePage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class GetThemeViewModel @Inject constructor(
    private val installedAppProvider: InstalledAppProvider,
    private val sharedPref: SharedPref,
    private val iconPackItemMapper: IconPackItemMapper,
    private val wallpaperRepository: WallpaperRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _iconsState = MutableStateFlow<UiState<List<IconPackItemUi>>>(UiState.Idle)
    val iconsState: StateFlow<UiState<List<IconPackItemUi>>> = _iconsState.asStateFlow()

    private val _selectedPage = MutableStateFlow(GetThemePage.WALLPAPERS)
    val selectedPage: StateFlow<GetThemePage> = _selectedPage.asStateFlow()

    private val _wallpaperDownloaded = MutableStateFlow(false)
    val wallpaperDownloaded: StateFlow<Boolean> = _wallpaperDownloaded.asStateFlow()

    private val _wallpaperDownloadState = MutableSharedFlow<DownloadState<File>>(
        replay = 1,
        extraBufferCapacity = 64,
    )
    val wallpaperDownloadState: SharedFlow<DownloadState<File>> = _wallpaperDownloadState.asSharedFlow()

    private val _applyWallpaperState = MutableSharedFlow<UiState<Unit>>()
    val applyWallpaperState: SharedFlow<UiState<Unit>> = _applyWallpaperState.asSharedFlow()

    var currentTheme: ThemeModel? = null
    private var currentWallpaperDownloadState: DownloadState<File> = DownloadState.Idle

    private var installedAppsByPackage: Map<String, AppInfo> = emptyMap()
    private var installedAppsByName: Map<String, AppInfo> = emptyMap()
    private val manualMappings = mutableMapOf<Long, AppInfo>()
    private val appliedIconIds = mutableSetOf<Long>()
    private var pendingIconId: Long? = null
    private var installingAllAfterUnlock = false

    var itemIconPackItemUi : IconPackItemUi? = null

    fun load(theme: ThemeModel) {
        currentTheme = theme
        checkWallpaperDownloaded(theme)
        viewModelScope.launch {
            _iconsState.value = UiState.Loading
            runCatching {
                val apps = installedAppProvider.getInstalledApps()
                installedAppsByPackage = apps.associateBy { it.packageName }
                installedAppsByName = apps.distinctBy { it.appName.normalizedAppName() }
                    .associateBy { it.appName.normalizedAppName() }
                theme.icons.mapIndexed { index, icon ->
                    IndexedIcon(
                        index = index,
                        item = mapIcon(icon),
                    )
                }.sortedWith(
                    compareBy<IndexedIcon> { it.item.initialOrderGroup() }
                        .thenBy { it.index }
                ).map { it.item }
            }.onSuccess { items ->
                _iconsState.value = UiState.Data(items)
            }.onFailure { throwable ->
                _iconsState.value = UiState.Error(
                    throwable = throwable,
                    message = throwable.message.orEmpty(),
                )
            }
        }
    }

    fun downloadWallpaper() {
        if (currentWallpaperDownloadState is DownloadState.Downloading ||
            currentWallpaperDownloadState is DownloadState.Saving
        ) {
            return
        }
        val wallpaper = currentTheme?.toWallpaperModel() ?: return
        viewModelScope.launch {
            wallpaperRepository.downloadWallpaper(wallpaper).collect { state ->
                currentWallpaperDownloadState = state
                _wallpaperDownloadState.emit(state)
                if (state is DownloadState.Success) {
                    _wallpaperDownloaded.value = true
                }
            }
        }
    }

    fun canDownloadCurrentWallpaper(): Boolean {
        val theme = currentTheme ?: return false
        return themeRepository.checkUnlockWallpaper(theme)
    }

    fun unlockCurrentWallpaper() {
        currentTheme?.let(themeRepository::unlockWallpaper)
    }

    fun selectPage(page: GetThemePage) {
        _selectedPage.value = page
    }

    fun applyWallpaper(target: WallpaperTarget) {
        val wallpaper = currentTheme?.toWallpaperModel() ?: return
        viewModelScope.launch {
            _applyWallpaperState.emit(UiState.Loading)
            wallpaperRepository.applyWallpaper(wallpaper, target)
                .onSuccess { _applyWallpaperState.emit(UiState.Data(Unit)) }
                .onFailure { throwable ->
                    _applyWallpaperState.emit(
                        UiState.Error(
                            throwable = throwable,
                            message = throwable.message.orEmpty(),
                        )
                    )
                }
        }
    }

    fun unlockIcon() {
        itemIconPackItemUi?.let { item ->
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
        val currentState = iconsState.value as? UiState.Data ?: return
        val iconsById = currentTheme?.icons.orEmpty().associateBy(iconPackItemMapper::stableId)
        _iconsState.value = UiState.Data(
            currentState.value.map { item ->
                iconsById[item.id]?.let(::mapIcon) ?: item
            }
        )
    }

    fun markIconApplied(iconId: Long) {
        appliedIconIds.add(iconId)
        updateSingleIcon(iconId)
    }

    fun markIconsApplied(iconIds: List<Long>) {
        appliedIconIds.addAll(iconIds)
        val currentState = iconsState.value as? UiState.Data ?: return
        _iconsState.value = UiState.Data(
            currentState.value.map { item ->
                if (item.id in iconIds) item.copy(isApplied = true) else item
            }
        )
    }

    fun mappedIconsPendingInstall(): List<IconPackItemUi> {
        return currentIconItems().filter { it.isInstallable && !it.isApplied }
    }

    fun currentIconItems(): List<IconPackItemUi> {
        val currentState = iconsState.value as? UiState.Data ?: return emptyList()
        return currentState.value
    }

    fun setPendingIcon(icon: IconPackItemUi) {
        pendingIconId = icon.id
    }

    fun applySelectedApp(app: AppInfo) {
        val iconId = pendingIconId ?: return
        manualMappings[iconId] = app
        pendingIconId = null
        updateSingleIcon(iconId)
    }

    private fun updateSingleIcon(iconId: Long) {
        val currentState = iconsState.value as? UiState.Data ?: return
        val currentTheme = currentTheme ?: return
        val sourceIcon = currentTheme.icons.firstOrNull { iconPackItemMapper.stableId(it) == iconId }
            ?: return
        val updatedItem = mapIcon(sourceIcon)
        _iconsState.value = UiState.Data(
            currentState.value.map { if (it.id == iconId) updatedItem else it }
        )
    }

    private fun mapIcon(icon: com.wavez.controllerwidget.data.model.icon.IconItemModel): IconPackItemUi {
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

    private fun checkWallpaperDownloaded(theme: ThemeModel) {
        viewModelScope.launch {
            _wallpaperDownloaded.value = wallpaperRepository.checkWallpaperDownloaded(theme.wallpaper.id)
        }
    }

    private fun ThemeModel.toWallpaperModel(): WallpaperModel {
        return WallpaperModel(
            id = wallpaper.id,
            thumb = wallpaper.thumb,
            premium = premium,
            url = wallpaper.url,
            categoryId = categoryId,
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
