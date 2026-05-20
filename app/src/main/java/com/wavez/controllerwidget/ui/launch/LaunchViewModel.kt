package com.wavez.controllerwidget.ui.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.data.local.InstalledAppProvider
import com.wavez.controllerwidget.data.repository.IconPackRepository
import com.wavez.controllerwidget.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val installedAppProvider: InstalledAppProvider,
    private val wallpaperRepository: WallpaperRepository,
    private val iconPackRepository: IconPackRepository
) : ViewModel() {


    fun preloadData() {
        viewModelScope.launch {

            launch(Dispatchers.IO) {
                installedAppProvider.getInstalledApps()
            }

            launch(Dispatchers.IO) {
                wallpaperRepository.getCategoriesUnlimited()
            }

            launch(Dispatchers.IO) {
                iconPackRepository.getIconPacks()
            }
        }
    }
}