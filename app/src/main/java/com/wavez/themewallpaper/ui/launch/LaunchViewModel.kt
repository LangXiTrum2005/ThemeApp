package com.wavez.themewallpaper.ui.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.data.local.InstalledAppProvider
import com.wavez.themewallpaper.data.repository.IconPackRepository
import com.wavez.themewallpaper.data.repository.ThemeRepository
import com.wavez.themewallpaper.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val installedAppProvider: InstalledAppProvider,
    private val wallpaperRepository: WallpaperRepository,
    private val iconPackRepository: IconPackRepository,
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    fun preloadData() {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                installedAppProvider.getInstalledApps()
            }

            launch {
                themeRepository.getThemeCategories()
            }

            launch {
                wallpaperRepository.getCategoriesUnlimited()
            }
        }
    }
}