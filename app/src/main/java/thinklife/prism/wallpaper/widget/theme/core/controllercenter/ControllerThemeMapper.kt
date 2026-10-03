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
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerConfigModel
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerTheme
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.MyControllerJsonConfig
import thinklife.prism.wallpaper.widget.theme.data.model.ControllerModel



private const val URL_PREFIX = "http"
private const val COLOR_PREFIX = "0x"
private const val DOUBLE_PNG_SUFFIX = ".png.png"
private const val PNG_SUFFIX = ".png"
private const val BOTTOM_DIY_BG_FILE = "home_bottom_diy_bg.png"
private const val SCREENSHOT_ICON_FILE = "home_bottom_ic_screenshot.png"
private const val FIRST_MISSING_SCREENSHOT_THEME_ID = 1
private const val LAST_MISSING_SCREENSHOT_THEME_ID = 32
private const val SPIDER_MAN_2_THEME_ID = "44"

// CDN thiếu ảnh screenshot của các theme này; ảnh record dự phòng sẽ làm hai biểu tượng chồng nhau.
internal fun hasScreenshotIcon(themeId: String): Boolean {
    val numericId = themeId.toIntOrNull()
    return themeId != SPIDER_MAN_2_THEME_ID &&
        (numericId == null || numericId !in FIRST_MISSING_SCREENSHOT_THEME_ID..LAST_MISSING_SCREENSHOT_THEME_ID)
}

fun ControllerModel.toControllerTheme() = ControllerTheme(
    id = id,
    name = name.ifBlank { fallbackName() },
    thumbnail = thumbnails,
    rank = rank,
)

/** Only URLs explicitly supplied by the data; the required background is first. */
fun ControllerModel.assetUrls(): List<String> = listOf(
    bgAll,
    flightOn, flightOff, airdropOn, airdropOff,
    bluetoothOn, bluetoothOff, wifiOn, wifiOff,
    rotateOn, rotateOff, muteOn, muteOff,
    flashOn, flashOff, batteryOff,
    recordOn, recordOff, settingOff,
    lockOn, lockOff, timeOff,
    calculatorOff, cameraOff,
    sound, mute, light, dark,
    back, next, play, pause,
    bgFunction, bgMusic, bgTime,
    bgLight, progressLight,
    bgVolume, progressVolume,
).filter { it.startsWith(URL_PREFIX) }.distinct()

fun ControllerModel.toConfig(context: Context): ControllerConfigModel {
    fun asset(url: String) = ControllerThemeAssets.resolve(context, id, url)
    return ControllerConfigModel(
        key = id,
        airplaneIcon = asset(flightOff),
        airplaneSelectedIcon = asset(flightOn),
        bluetoothIcon = asset(bluetoothOff),
        bluetoothSelectedIcon = asset(bluetoothOn),
        mobileDataIcon = asset(airdropOff),
        mobileDataSelectedIcon = asset(airdropOn),
        wifiIcon = asset(wifiOff),
        wifiSelectedIcon = asset(wifiOn),
        soundBackground = asset(bgVolume),
        soundBigIcon = asset(sound),
        soundMuteIcon = asset(mute),
        soundSlider = asset(progressVolume),
        lightBackground = asset(bgLight),
        lightSlider = asset(progressLight),
        lightIcon = asset(light),
        lightHomeIcon = asset(light),
        musicNextIcon = asset(next),
        musicPauseIcon = asset(pause),
        musicPlayIcon = asset(play),
        musicPreviousIcon = asset(back),
        musicBackground = asset(bgMusic),
        homeBackground = asset(bgAll),
        homeControlBackground = asset(bgFunction),
        homeMusicBackground = asset(bgMusic),
        controlBackground = asset(bgFunction),
        bottomDiyBackground = themeAsset(BOTTOM_DIY_BG_FILE),
        batteryIcon = asset(batteryOff),
        calculatorIcon = asset(calculatorOff),
        cameraIcon = asset(cameraOff),
        screenshotIcon = themeAsset(SCREENSHOT_ICON_FILE),
        showScreenshotButton = hasScreenshotIcon(id),
        setupIcon = asset(settingOff),
        timingIcon = asset(timeOff),
        flashlightIcon = asset(flashOff),
        flashlightSelectedIcon = asset(flashOn),
        recordIcon = asset(recordOff),
        recordSelectedIcon = asset(recordOn),
        recordingIcon = asset(recordOff),
        focusIcon = asset(bgTime),
        focusSelectedIcon = asset(bgTime),
        lockingIcon = asset(lockOff),
        lockingSelectedIcon = asset(lockOn),
        ringIcon = asset(muteOff),
        ringSelectedIcon = asset(muteOn),
        controllerJsonConfig = MyControllerJsonConfig(
            controlTextColor = screentimeFontColor.toHexColor(),
            volumeTextColor = screentimeFontColor.toHexColor(),
            brightnessTextColor = screentimeFontColor.toHexColor(),
            musicKnownTitleColor = musicFontColor.toHexColor(),
            musicKnownSingerColor = musicFontColor.toHexColor(),
            musicUnknownTitleColor = musicFontColor.toHexColor(),
            musicUnknownSingerColor = musicFontColor.toHexColor(),
            musicTimeColor = musicFontColor.toHexColor(),
        ),
    )
}

// Data không có tên riêng: lấy tên bộ sưu tập trong path thumbnail (".../Spider-Man/01/thumb.png").
private fun ControllerModel.fallbackName(): String {
    val pathParts = thumbnails.substringBefore('?').split('/')
    val collection = pathParts.getOrNull(pathParts.lastIndex - 2)
    return collection?.replace('-', ' ')?.plus(" $rank") ?: "Theme $rank"
}

private fun String.toHexColor(): String =
    removePrefix(COLOR_PREFIX).removePrefix(COLOR_PREFIX.uppercase()).let { "#$it" }

// Hai ảnh này không có field trong data nhưng nằm cùng thư mục với bg_all trên CDN.
private fun ControllerModel.themeAsset(fileName: String): String =
    bgAll.substringBeforeLast('/') + "/$fileName" +
        if (bgAll.endsWith(DOUBLE_PNG_SUFFIX)) PNG_SUFFIX else ""
