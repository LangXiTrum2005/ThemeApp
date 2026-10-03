/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.core.controllercenter

import android.content.Context
import com.google.gson.Gson
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerConfigModel
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerTheme
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.MyControllerJsonConfig


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
        load(context).firstOrNull { it.control_id == themeId }?.toConfig(context)

    fun theme(context: Context, themeId: String): ControllerTheme? =
        themes(context).firstOrNull { it.id == themeId }

    /** Only URLs explicitly supplied by control_data.json; the required background is first. */
    fun assetUrls(context: Context, themeId: String): List<String> {
        val theme = load(context).firstOrNull { it.control_id == themeId } ?: return emptyList()
        return listOf(
            theme.bg_all,
            theme.flight_on, theme.flight_off, theme.airdrop_on, theme.airdrop_off,
            theme.bluetooth_on, theme.bluetooth_off, theme.wifi_on, theme.wifi_off,
            theme.rotate_on, theme.rotate_off, theme.mute_on, theme.mute_off,
            theme.flash_on, theme.flash_off, theme.battery_off,
            theme.record_on, theme.record_off, theme.setting_off,
            theme.lock_on, theme.lock_off, theme.time_off,
            theme.calculator_off, theme.camera_off,
            theme.sound, theme.mute, theme.light, theme.dark,
            theme.back, theme.next, theme.play, theme.pause,
            theme.bg_function, theme.bg_music, theme.bg_time,
            theme.bg_light, theme.progress_light,
            theme.bg_volume, theme.progress_volume,
        ).filter { it.startsWith("http") }.distinct()
    }

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

    private fun ControllerThemeData.toConfig(context: Context): ControllerConfigModel {
        fun asset(url: String) = ControllerThemeAssets.resolve(context, control_id, url)
        val resolvedLockOff = if (lock_off.contains("Screen Mirroring", ignoreCase = true) || lock_off.isBlank()) {
            if (rotate_off.isNotBlank()) rotate_off else lock_off
        } else {
            lock_off
        }
        val resolvedLockOn = if (lock_on.contains("Screen Mirroring", ignoreCase = true) || lock_on.isBlank()) {
            if (rotate_on.isNotBlank()) rotate_on else lock_on
        } else {
            lock_on
        }
        val resolvedRotateOff = if (rotate_off.isNotBlank()) rotate_off else lock_off
        val resolvedRotateOn = if (rotate_on.isNotBlank()) rotate_on else lock_on
        return ControllerConfigModel(
        key = control_id,
        airplaneIcon = asset(flight_off),
        airplaneSelectedIcon = asset(flight_on),
        bluetoothIcon = asset(bluetooth_off),
        bluetoothSelectedIcon = asset(bluetooth_on),
        mobileDataIcon = asset(airdrop_off),
        mobileDataSelectedIcon = asset(airdrop_on),
        wifiIcon = asset(wifi_off),
        wifiSelectedIcon = asset(wifi_on),
        soundBackground = asset(bg_volume),
        soundBigIcon = asset(sound),
        soundMuteIcon = asset(mute),
        soundSlider = asset(progress_volume),
        lightBackground = asset(bg_light),
        lightSlider = asset(progress_light),
        lightIcon = asset(light),
        lightHomeIcon = asset(light),
        musicNextIcon = asset(next),
        musicPauseIcon = asset(pause),
        musicPlayIcon = asset(play),
        musicPreviousIcon = asset(back),
        musicBackground = asset(bg_music),
        homeBackground = asset(bg_all),
        homeControlBackground = asset(bg_function),
        homeMusicBackground = asset(bg_music),
        controlBackground = asset(bg_function),
        bottomDiyBackground = themeAsset("home_bottom_diy_bg.png"),
        batteryIcon = asset(battery_off),
        calculatorIcon = asset(calculator_off),
        cameraIcon = asset(camera_off),
        screenshotIcon = themeAsset("home_bottom_ic_screenshot.png"),
        showScreenshotButton = hasScreenshotIcon(control_id),
        screenLockIcon = asset(resolvedLockOff),
        screenLockSelectedIcon = asset(resolvedLockOn),
        setupIcon = asset(setting_off),
        timingIcon = asset(time_off),
        flashlightIcon = asset(flash_off),
        flashlightSelectedIcon = asset(flash_on),
        recordIcon = asset(record_off),
        recordSelectedIcon = asset(record_on),
        recordingIcon = asset(record_off),
        focusIcon = asset(bg_time),
        focusSelectedIcon = asset(bg_time),
        lockingIcon = asset(resolvedRotateOff),
        lockingSelectedIcon = asset(resolvedRotateOn),
        ringIcon = asset(mute_off),
        ringSelectedIcon = asset(mute_on),
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
    }

    private fun String.toHexColor(): String =
        removePrefix("0x").removePrefix("0X").let { "#$it" }

    private fun ControllerThemeData.themeAsset(fileName: String): String =
        bg_all.substringBeforeLast('/') + "/$fileName" +
            if (bg_all.endsWith(".png.png")) ".png" else ""
}
