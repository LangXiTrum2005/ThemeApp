package com.wavez.controllerwidget.ui.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.ads.AdsId
import com.wavez.controllerwidget.ads.native_ad.NativeAdPlacement
import com.wavez.controllerwidget.data.local.InstalledAppProvider
import com.wavez.controllerwidget.data.repository.IconPackRepository
import com.wavez.controllerwidget.data.repository.SharedPref
import com.wavez.controllerwidget.data.repository.ThemeRepository
import com.wavez.controllerwidget.data.repository.WallpaperRepository
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
    private val sharedPref: SharedPref
) : ViewModel() {

    private fun entryWithAdPlacement(
        adIds: List<String>,
        show: Boolean
    ) = adIds to show

    private fun entryNativeInApp(show: Boolean) = AdsId.getNativeInAppAds() to show

    fun initNativeAd(): Map<NativeAdPlacement, Pair<List<String>, Boolean>> = mapOf(
        NativeAdPlacement.LANGUAGE to entryWithAdPlacement(
            AdsId.getListAdNativeLanguageAds1(),
            sharedPref.shouldShowNativeLanguage1
        ),
        NativeAdPlacement.LANGUAGE_2 to entryWithAdPlacement(
            AdsId.getListAdNativeLanguageAds2(),
            sharedPref.shouldShowNativeLanguage2
        ),
        NativeAdPlacement.ONBOARDING1 to entryWithAdPlacement(
            AdsId.getListAdNativeOnboarding1(),
            sharedPref.shouldShowNativeObd1
        ),
        NativeAdPlacement.ONBOARDING1_CLICK to entryWithAdPlacement(
            AdsId.getListAdNativeOnboarding1Click(),
            sharedPref.shouldShowNativeObd1Click
        ),
        NativeAdPlacement.ONBOARDING2 to entryWithAdPlacement(
            AdsId.getListAdNativeOnboarding2(),
            sharedPref.shouldShowNativeObd2
        ),
        NativeAdPlacement.ONBOARDING3 to entryWithAdPlacement(
            AdsId.getListAdNativeOnboarding3(),
            sharedPref.shouldShowNativeObd3
        ),
        NativeAdPlacement.NATIVE_HOME to entryWithAdPlacement(
            AdsId.getNativeHomeAds(),
            sharedPref.shouldShowNativeHome
        ),
        NativeAdPlacement.NATIVE_BANNER_DETAIL to entryNativeInApp(
            sharedPref.shouldShowNativeInApp
        ),
        NativeAdPlacement.NATIVE_THEME_APPLIED to entryNativeInApp(
            sharedPref.shouldShowNativeInApp
        ),
        NativeAdPlacement.NATIVE_WALLPAPER_APPLIED to entryNativeInApp(
            sharedPref.shouldShowNativeInApp
        ),
        NativeAdPlacement.NATIVE_ICON_APPLIED to entryNativeInApp(
            sharedPref.shouldShowNativeInApp
        ),
    )


    fun preloadData() {
        viewModelScope.launch {

            launch(Dispatchers.IO) {
                installedAppProvider.getInstalledApps()
            }

            launch(Dispatchers.IO) {
                wallpaperRepository.getCategoriesUnlimited()
            }

            launch {
                themeRepository.getThemeCategories()
            }

            launch(Dispatchers.IO) {
                iconPackRepository.getIconPacks()
            }
        }
    }
}