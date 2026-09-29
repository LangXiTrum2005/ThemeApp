package com.wavez.themewallpaper.core.controllercenter

import android.content.Context
import com.google.gson.Gson
import com.wavez.themewallpaper.core.controllercenter.model.ControllerConfigModel
import com.wavez.themewallpaper.core.controllercenter.model.MyControllerJsonConfig

data class ControllerTheme(
    val id: String,
    val name: String,
    val thumbnail: String,
    val rank: Int,
)

private data class ControllerThemeData(
    val control_id: String,
    val control_name: String = "",
    val rank: Int,
    val thumbnails: String,
    val flight_on: String,
    val flight_off: String,
    val airdrop_on: String,
    val airdrop_off: String,
    val bluetooth_on: String,
    val bluetooth_off: String,
    val wifi_on: String,
    val wifi_off: String,
    val rotate_on: String,
    val rotate_off: String,
    val mute_on: String,
    val mute_off: String,
    val flash_on: String,
    val flash_off: String,
    val battery_off: String,
    val record_on: String,
    val record_off: String,
    val setting_off: String,
    val lock_on: String,
    val lock_off: String,
    val time_off: String,
    val calculator_off: String,
    val camera_off: String,
    val sound: String,
    val mute: String,
    val light: String,
    val dark: String,
    val back: String,
    val next: String,
    val play: String,
    val pause: String,
    val bg_function: String,
    val bg_music: String,
    val bg_time: String,
    val bg_light: String,
    val progress_light: String,
    val bg_volume: String,
    val progress_volume: String,
    val bg_all: String,
    val music_font_color: String,
    val screentime_font_color: String,
)

object ControllerThemeCatalog {
    private const val ASSET_FILE = "control_data.json"

    @Volatile
    private var cachedThemes: List<ControllerThemeData>? = null

    fun themes(context: Context): List<ControllerTheme> = load(context).map { theme ->
        ControllerTheme(
            id = theme.control_id,
            name = theme.control_name.ifBlank { themeName(theme) },
            thumbnail = theme.thumbnails,
            rank = theme.rank,
        )
    }

    fun config(context: Context, themeId: String): ControllerConfigModel? =
        load(context).firstOrNull { it.control_id == themeId }?.toConfig()

    fun theme(context: Context, themeId: String): ControllerTheme? =
        themes(context).firstOrNull { it.id == themeId }

    private fun load(context: Context): List<ControllerThemeData> = cachedThemes ?: synchronized(this) {
        cachedThemes ?: context.assets.open(ASSET_FILE).bufferedReader().use { reader ->
            Gson().fromJson(reader, Array<ControllerThemeData>::class.java)
                .orEmpty()
                .sortedBy { it.rank }
                .toList()
                .also { cachedThemes = it }
        }
    }

    private fun themeName(theme: ControllerThemeData): String {
        val pathParts = theme.thumbnails.substringBefore('?').split('/')
        val collection = pathParts.getOrNull(pathParts.lastIndex - 2)
        return collection?.replace('-', ' ')?.plus(" ${theme.rank}") ?: "Theme ${theme.rank}"
    }

    private fun ControllerThemeData.toConfig() = ControllerConfigModel(
        key = control_id,
        airplaneIcon = flight_off,
        airplaneSelectedIcon = flight_on,
        bluetoothIcon = bluetooth_off,
        bluetoothSelectedIcon = bluetooth_on,
        mobileDataIcon = airdrop_off,
        mobileDataSelectedIcon = airdrop_on,
        wifiIcon = wifi_off,
        wifiSelectedIcon = wifi_on,
        soundBackground = bg_volume,
        soundBigIcon = sound,
        soundMuteIcon = mute,
        soundSlider = progress_volume,
        lightBackground = bg_light,
        lightSlider = progress_light,
        lightIcon = light,
        lightHomeIcon = light,
        musicNextIcon = next,
        musicPauseIcon = pause,
        musicPlayIcon = play,
        musicPreviousIcon = back,
        musicBackground = bg_music,
        homeBackground = bg_all,
        homeControlBackground = bg_function,
        homeMusicBackground = bg_music,
        controlBackground = bg_function,
        bottomDiyBackground = themeAsset("home_bottom_diy_bg.png"),
        batteryIcon = battery_off,
        calculatorIcon = calculator_off,
        cameraIcon = camera_off,
        screenshotIcon = themeAsset("home_bottom_ic_screenshot.png"),
        setupIcon = setting_off,
        timingIcon = time_off,
        flashlightIcon = flash_off,
        flashlightSelectedIcon = flash_on,
        recordIcon = record_off,
        recordSelectedIcon = record_on,
        recordingIcon = record_off,
        focusIcon = bg_time,
        focusSelectedIcon = bg_time,
        lockingIcon = lock_off,
        lockingSelectedIcon = lock_on,
        ringIcon = mute_off,
        ringSelectedIcon = mute_on,
        controllerJsonConfig = MyControllerJsonConfig(
            controlTextColor = screentime_font_color.toHexColor(),
            volumeTextColor = screentime_font_color.toHexColor(),
            brightnessTextColor = screentime_font_color.toHexColor(),
            musicKnownTitleColor = music_font_color.toHexColor(),
            musicKnownSingerColor = music_font_color.toHexColor(),
            musicUnknownTitleColor = music_font_color.toHexColor(),
            musicUnknownSingerColor = music_font_color.toHexColor(),
            musicTimeColor = music_font_color.toHexColor(),
        ),
    )

    private fun String.toHexColor(): String =
        removePrefix("0x").removePrefix("0X").let { "#$it" }

    private fun ControllerThemeData.themeAsset(fileName: String): String =
        bg_all.substringBeforeLast('/') + "/$fileName" +
            if (bg_all.endsWith(".png.png")) ".png" else ""
}
