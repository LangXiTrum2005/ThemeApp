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
        NativeAdPlacement.ONBOARDING_FULL_1 to entryWithAdPlacement(
            AdsId.getListAdNativeObdFull1(),
            sharedPref.shouldShowNativeAdFull1
        ),
        NativeAdPlacement.ONBOARDING_FULL_2 to entryWithAdPlacement(
            AdsId.getListAdNativeObdFull2(),
            sharedPref.shouldShowNativeAdFull2
        ),
        NativeAdPlacement.NATIVE_HOME to entryWithAdPlacement(
            AdsId.getNativeHomeAds(),
            sharedPref.shouldShowNativeHome
        ),
        NativeAdPlacement.NATIVE_BOOST_SUCCESS to entryWithAdPlacement(
            AdsId.getNativeBoostSuccessAds(),
            sharedPref.shouldShowNativeBoostSuccess
        ),
        NativeAdPlacement.NATIVE_CONFIG_1 to entryWithAdPlacement(
            AdsId.getNativeConfig1(),
            sharedPref.shouldShowNativeConfig
        ),
        NativeAdPlacement.NATIVE_CONFIG_2 to entryWithAdPlacement(
            AdsId.getNativeConfig2(),
            sharedPref.shouldShowNativeConfig
        ),
        NativeAdPlacement.NATIVE_CONFIG_3 to entryWithAdPlacement(
            AdsId.getNativeConfig3(),
            sharedPref.shouldShowNativeConfig
        ),
        NativeAdPlacement.NATIVE_CONFIG_4 to entryWithAdPlacement(
            AdsId.getNativeConfig4(),
            sharedPref.shouldShowNativeConfig
        ),
        NativeAdPlacement.NATIVE_SELECT_APP to entryWithAdPlacement(
            AdsId.getNativeSelectApp(),
            sharedPref.shouldShowNativeSelectApp
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