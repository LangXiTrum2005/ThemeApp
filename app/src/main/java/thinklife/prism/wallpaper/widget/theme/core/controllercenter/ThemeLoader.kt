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
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.MyControllerJsonConfig
import thinklife.prism.wallpaper.widget.theme.data.local.ThemeStorageConfig
import java.io.File

object ThemeLoader {

    private object Assets {
        // Config
        const val CONFIG_JSON = "control_spec.json"

        // Network
        const val AIRPLANE             = "common_control_ic_airplane.png"
        const val AIRPLANE_SELECTED    = "common_control_ic_airplane_select.png"
        const val BLUETOOTH            = "common_control_ic_bluetooth.png"
        const val BLUETOOTH_SELECTED   = "common_control_ic_bluetooth_select.png"
        const val MOBILE_DATA          = "common_control_ic_data.png"
        const val MOBILE_DATA_SELECTED = "common_control_ic_data_select.png"
        const val WIFI                 = "common_control_wifi.png"
        const val WIFI_SELECTED        = "common_control_wifi_select.png"
        const val HOTSPOT              = "control_ic_hotspot.png"
        const val HOTSPOT_SELECTED     = "control_ic_hotspot_select.png"
        const val SYNC                 = "control_ic_sync.png"
        const val SYNC_SELECTED        = "control_ic_sync_select.png"
        const val CONTROL_BACKGROUND   = "control_bg.png"

        // Sound / Volume
        const val SOUND_BACKGROUND = "common_home_sound_bg.png"
        const val SOUND_BIG_ICON   = "common_home_sound_big.png"
        const val SOUND_MUTE_ICON  = "common_home_sound_mute.png"
        const val SOUND_SLIDER     = "common_home_sound_slide.png"

        // Brightness
        const val LIGHT_BACKGROUND             = "common_light_bg.png"
        const val LIGHT_SLIDER                 = "common_light_slide.png"
        const val LIGHT_ICON                   = "light_ic_light.png"
        const val LIGHT_HOME_ICON              = "home_light_icon.png"
        const val LIGHT_AUTO_BRIGHT_ICON       = "light_ic_auto_bright.png"
        const val LIGHT_AUTO_BRIGHT_SELECTED   = "light_ic_auto_bright_select.png"
        const val LIGHT_NIGHT_SHIFT_ICON       = "light_ic_night_shift.png"
        const val LIGHT_NIGHT_SHIFT_SELECTED   = "light_ic_night_shift_select.png"

        // Music basic
        const val MUSIC_NEXT     = "common_music_ic_next.png"
        const val MUSIC_PAUSE    = "common_music_ic_pause.png"
        const val MUSIC_PLAY     = "common_music_ic_play.png"
        const val MUSIC_PREVIOUS = "common_music_ic_previous.png"

        // Music extended
        const val MUSIC_BACKGROUND        = "music_bg.png"
        const val ALBUM_DEFAULT_IMAGE     = "music_im_default.png"
        const val MUSIC_PROGRESS_BG       = "music_bg_music_progress.png"
        const val MUSIC_PROGRESS_ICON     = "music_ic_music_progress.png"
        const val MUSIC_PROGRESS_IMAGE    = "music_im_music_progress.png"
        const val SOUND_PROGRESS_BG       = "music_bg_sound_progress.png"
        const val SOUND_PROGRESS_ICON     = "music_ic_sound_progress.png"
        const val SOUND_PROGRESS_IMAGE    = "music_im_sound_progress.png"

        // Home backgrounds
        const val HOME_BACKGROUND       = "home_bg.jpg"
        const val HOME_CONTROL_BG       = "home_control_bg.png"
        const val HOME_MUSIC_BG         = "home_music_bg.png"
        const val BOTTOM_DIY_BG         = "home_bottom_diy_bg.png"

        // Bottom action icons
        const val BATTERY    = "home_bottom_ic_battery.png"
        const val CALCULATOR = "home_bottom_ic_calculator.png"
        const val CAMERA     = "home_bottom_ic_camera.png"
        const val SCREENSHOT = "home_bottom_ic_screenshot.png"
        const val SETUP      = "home_bottom_ic_setup.png"
        const val TIMING     = "home_bottom_ic_timing.png"

        // Flashlight
        const val FLASHLIGHT          = "home_bottom_ic_flashlight.png"
        const val FLASHLIGHT_SELECTED = "home_bottom_ic_flashlight_select.png"
        const val FLASHLIGHT_OPEN     = "home_bottom_ic_flashlight_open.pag"
        const val FLASHLIGHT_CLOSE    = "home_bottom_ic_flashlight_close.pag"

        // Record
        const val RECORD          = "home_bottom_ic_record.png"
        const val RECORD_SELECTED = "home_bottom_ic_record_select.png"
        const val RECORD_OPEN     = "home_bottom_ic_record_open.pag"
        const val RECORD_CLOSE    = "home_bottom_ic_record_close.pag"
        const val RECORDING       = "home_bottom_ic_recording.png"

        // Focus / auto-rotate
        const val FOCUS          = "home_second_ic_focus.png"
        const val FOCUS_SELECTED = "home_second_ic_focus_select.png"
        const val FOCUS_OPEN     = "home_second_ic_focus_open.pag"
        const val FOCUS_CLOSE    = "home_second_ic_focus_close.pag"

        // Locking / DND
        const val LOCKING          = "home_second_ic_locking.png"
        const val LOCKING_SELECTED = "home_second_ic_locking_select.png"
        const val LOCKING_OPEN     = "home_second_ic_locking_open.pag"
        const val LOCKING_CLOSE    = "home_second_ic_locking_close.pag"

        // Ring / Mute
        const val RING          = "home_second_ic_ring.png"
        const val RING_SELECTED = "home_second_ic_ring_select.png"
        const val RING_OPEN     = "home_second_ic_ring_open.pag"
        const val RING_CLOSE    = "home_second_ic_ring_close.pag"
    }

    private object JsonKeys {
        const val PULL_BAR_COLOR                = "pullBarColor"
        const val PULL_BAR_ALPHA                = "pullBarAlpha"
        const val CONTROL_TEXT_COLOR            = "controlTextColor"
        const val CONTROL_TEXT_ALPHA            = "controlTextAlpha"
        const val VOLUME_TEXT_COLOR             = "volumeTextColor"
        const val VOLUME_TEXT_ALPHA             = "volumeTextAlpha"
        const val BRIGHTNESS_TEXT_COLOR         = "brightnessTextColor"
        const val BRIGHTNESS_TEXT_ALPHA         = "brightnessTextAlpha"
        const val MUSIC_KNOWN_TITLE_COLOR       = "musicKnownTitleColor"
        const val MUSIC_KNOWN_TITLE_ALPHA       = "musicKnownTitleAlpha"
        const val MUSIC_KNOWN_SINGER_COLOR      = "musicKnownSingerColor"
        const val MUSIC_KNOWN_SINGER_ALPHA      = "musicKnownSingerAlpha"
        const val MUSIC_UNKNOWN_TITLE_COLOR     = "musicUnknownTitleColor"
        const val MUSIC_UNKNOWN_TITLE_ALPHA     = "musicUnknownTitleAlpha"
        const val MUSIC_UNKNOWN_SINGER_COLOR    = "musicUnknownSingerColor"
        const val MUSIC_UNKNOWN_SINGER_ALPHA    = "musicUnknownSingerAlpha"
        const val MUSIC_TIME_COLOR              = "musicTimeColor"
        const val MUSIC_TIME_ALPHA              = "musicTimeAlpha"
        const val MUSIC_ALBUM_CORNER            = "musicAlbumCorner"
        const val HOME_CONTROL_DIY_CORNER       = "homeControlDiyCorner"
        const val HOME_CONTROL_DIY_SIZE         = "homeControlDiySize"
    }

    // ─── Public API ───────────────────────────────────────────────

    val REQUIRED_ASSETS: List<String> = listOf(
        Assets.CONFIG_JSON,
        Assets.AIRPLANE, Assets.AIRPLANE_SELECTED,
        Assets.BLUETOOTH, Assets.BLUETOOTH_SELECTED,
        Assets.MOBILE_DATA, Assets.MOBILE_DATA_SELECTED,
        Assets.WIFI, Assets.WIFI_SELECTED,
        Assets.HOTSPOT, Assets.HOTSPOT_SELECTED,
        Assets.SYNC, Assets.SYNC_SELECTED,
        Assets.CONTROL_BACKGROUND,
        Assets.SOUND_BACKGROUND, Assets.SOUND_BIG_ICON, Assets.SOUND_MUTE_ICON, Assets.SOUND_SLIDER,
        Assets.LIGHT_BACKGROUND, Assets.LIGHT_SLIDER, Assets.LIGHT_ICON, Assets.LIGHT_HOME_ICON,
        Assets.LIGHT_AUTO_BRIGHT_ICON, Assets.LIGHT_AUTO_BRIGHT_SELECTED,
        Assets.LIGHT_NIGHT_SHIFT_ICON, Assets.LIGHT_NIGHT_SHIFT_SELECTED,
        Assets.MUSIC_NEXT, Assets.MUSIC_PAUSE, Assets.MUSIC_PLAY, Assets.MUSIC_PREVIOUS,
        Assets.MUSIC_BACKGROUND, Assets.ALBUM_DEFAULT_IMAGE,
        Assets.MUSIC_PROGRESS_BG, Assets.MUSIC_PROGRESS_ICON, Assets.MUSIC_PROGRESS_IMAGE,
        Assets.SOUND_PROGRESS_BG, Assets.SOUND_PROGRESS_ICON, Assets.SOUND_PROGRESS_IMAGE,
        Assets.HOME_BACKGROUND, Assets.HOME_CONTROL_BG, Assets.HOME_MUSIC_BG, Assets.BOTTOM_DIY_BG,
        Assets.BATTERY, Assets.CALCULATOR, Assets.CAMERA,
        Assets.SCREENSHOT, Assets.SETUP, Assets.TIMING,
        Assets.FLASHLIGHT, Assets.FLASHLIGHT_SELECTED, Assets.FLASHLIGHT_OPEN, Assets.FLASHLIGHT_CLOSE,
        Assets.RECORD, Assets.RECORD_SELECTED, Assets.RECORD_OPEN, Assets.RECORD_CLOSE, Assets.RECORDING,
        Assets.FOCUS, Assets.FOCUS_SELECTED, Assets.FOCUS_OPEN, Assets.FOCUS_CLOSE,
        Assets.LOCKING, Assets.LOCKING_SELECTED, Assets.LOCKING_OPEN, Assets.LOCKING_CLOSE,
        Assets.RING, Assets.RING_SELECTED, Assets.RING_OPEN, Assets.RING_CLOSE,
    )

    fun isReady(context: Context, themeName: String = ThemeStorageConfig.DEFAULT_THEME_NAME): Boolean {
        val dir = themeDir(context, themeName)
        return REQUIRED_ASSETS.all { File(dir, it).exists() }
    }

    fun missingAssets(context: Context, themeName: String = ThemeStorageConfig.DEFAULT_THEME_NAME): List<String> {
        val dir = themeDir(context, themeName)
        return REQUIRED_ASSETS.filter { !File(dir, it).exists() }
    }

    fun load(context: Context, themeName: String = ThemeStorageConfig.DEFAULT_THEME_NAME): ControllerConfigModel {
        return loadFromDirectory(themeDir(context, themeName), themeName)
    }

    fun loadFromDirectory(dir: File, themeKey: String = dir.name): ControllerConfigModel {
        val missing = REQUIRED_ASSETS.filter { !File(dir, it).exists() }
        if (missing.isNotEmpty()) {
            android.util.Log.w("ThemeLoader", "Missing ${missing.size} assets: ${missing.take(3)}...")
        }
        fun p(name: String) = File(dir, name).absolutePath

        val jsonConfig = loadJsonConfig(File(dir, Assets.CONFIG_JSON))

        return ControllerConfigModel(
            key = themeKey,

            // Network
            airplaneIcon              = p(Assets.AIRPLANE),
            airplaneSelectedIcon      = p(Assets.AIRPLANE_SELECTED),
            bluetoothIcon             = p(Assets.BLUETOOTH),
            bluetoothSelectedIcon     = p(Assets.BLUETOOTH_SELECTED),
            mobileDataIcon            = p(Assets.MOBILE_DATA),
            mobileDataSelectedIcon    = p(Assets.MOBILE_DATA_SELECTED),
            wifiIcon                  = p(Assets.WIFI),
            wifiSelectedIcon          = p(Assets.WIFI_SELECTED),
            hotspotIcon               = p(Assets.HOTSPOT),
            hotspotSelectedIcon       = p(Assets.HOTSPOT_SELECTED),
            syncIcon                  = p(Assets.SYNC),
            syncSelectedIcon          = p(Assets.SYNC_SELECTED),
            controlBackground         = p(Assets.CONTROL_BACKGROUND),

            // Sound / Volume
            soundBackground = p(Assets.SOUND_BACKGROUND),
            soundBigIcon    = p(Assets.SOUND_BIG_ICON),
            soundMuteIcon   = p(Assets.SOUND_MUTE_ICON),
            soundSlider     = p(Assets.SOUND_SLIDER),

            // Brightness
            lightBackground             = p(Assets.LIGHT_BACKGROUND),
            lightSlider                 = p(Assets.LIGHT_SLIDER),
            lightIcon                   = p(Assets.LIGHT_ICON),
            lightHomeIcon               = p(Assets.LIGHT_HOME_ICON),
            lightAutoBrightIcon         = p(Assets.LIGHT_AUTO_BRIGHT_ICON),
            lightAutoBrightSelectedIcon = p(Assets.LIGHT_AUTO_BRIGHT_SELECTED),
            lightNightShiftIcon         = p(Assets.LIGHT_NIGHT_SHIFT_ICON),
            lightNightShiftSelectedIcon = p(Assets.LIGHT_NIGHT_SHIFT_SELECTED),

            // Music basic
            musicNextIcon     = p(Assets.MUSIC_NEXT),
            musicPauseIcon    = p(Assets.MUSIC_PAUSE),
            musicPlayIcon     = p(Assets.MUSIC_PLAY),
            musicPreviousIcon = p(Assets.MUSIC_PREVIOUS),

            // Music extended
            musicBackground       = p(Assets.MUSIC_BACKGROUND),
            albumDefaultImage     = p(Assets.ALBUM_DEFAULT_IMAGE),
            musicProgressBackground = p(Assets.MUSIC_PROGRESS_BG),
            musicProgressIcon     = p(Assets.MUSIC_PROGRESS_ICON),
            musicProgressImage    = p(Assets.MUSIC_PROGRESS_IMAGE),
            soundProgressBackground = p(Assets.SOUND_PROGRESS_BG),
            soundProgressIcon     = p(Assets.SOUND_PROGRESS_ICON),
            soundProgressImage    = p(Assets.SOUND_PROGRESS_IMAGE),

            // Home backgrounds
            homeBackground      = p(Assets.HOME_BACKGROUND),
            homeControlBackground = p(Assets.HOME_CONTROL_BG),
            homeMusicBackground = p(Assets.HOME_MUSIC_BG),
            bottomDiyBackground = p(Assets.BOTTOM_DIY_BG),

            // Bottom action icons
            batteryIcon    = p(Assets.BATTERY),
            calculatorIcon = p(Assets.CALCULATOR),
            cameraIcon     = p(Assets.CAMERA),
            screenshotIcon = p(Assets.SCREENSHOT),
            setupIcon      = p(Assets.SETUP),
            timingIcon     = p(Assets.TIMING),

            // Flashlight
            flashlightIcon          = p(Assets.FLASHLIGHT),
            flashlightSelectedIcon  = p(Assets.FLASHLIGHT_SELECTED),
            flashlightOpenAnimation = p(Assets.FLASHLIGHT_OPEN),
            flashlightCloseAnimation = p(Assets.FLASHLIGHT_CLOSE),

            // Record
            recordIcon          = p(Assets.RECORD),
            recordSelectedIcon  = p(Assets.RECORD_SELECTED),
            recordOpenAnimation = p(Assets.RECORD_OPEN),
            recordCloseAnimation = p(Assets.RECORD_CLOSE),
            recordingIcon       = p(Assets.RECORDING),

            // Focus / auto-rotate
            focusIcon          = p(Assets.FOCUS),
            focusSelectedIcon  = p(Assets.FOCUS_SELECTED),
            focusOpenAnimation = p(Assets.FOCUS_OPEN),
            focusCloseAnimation = p(Assets.FOCUS_CLOSE),

            // Locking / DND
            lockingIcon          = p(Assets.LOCKING),
            lockingSelectedIcon  = p(Assets.LOCKING_SELECTED),
            lockingOpenAnimation = p(Assets.LOCKING_OPEN),
            lockingCloseAnimation = p(Assets.LOCKING_CLOSE),

            // Ring / Mute
            ringIcon          = p(Assets.RING),
            ringSelectedIcon  = p(Assets.RING_SELECTED),
            ringOpenAnimation = p(Assets.RING_OPEN),
            ringCloseAnimation = p(Assets.RING_CLOSE),

            controllerJsonConfig = jsonConfig
        )
    }

    // ─── Private helpers ──────────────────────────────────────────

    private fun themeDir(context: Context, themeName: String): File =
        File(context.filesDir, "${ThemeStorageConfig.THEME_BASE_DIR}/$themeName")

    private fun loadJsonConfig(file: File): MyControllerJsonConfig {
        if (!file.exists()) return MyControllerJsonConfig()
        return try {
            parseJsonConfig(file.readText())
        } catch (_: Exception) {
            MyControllerJsonConfig()
        }
    }

    private fun parseJsonConfig(json: String): MyControllerJsonConfig {
        fun str(key: String, default: String) =
            Regex("\"$key\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: default
        fun flt(key: String, default: Float) =
            Regex("\"$key\"\\s*:\\s*([0-9.]+)").find(json)?.groupValues?.get(1)?.toFloatOrNull() ?: default
        fun int(key: String, default: Int) =
            Regex("\"$key\"\\s*:\\s*([0-9]+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: default

        return MyControllerJsonConfig(
            pullBarColor              = str(JsonKeys.PULL_BAR_COLOR,             "#000000"),
            pullBarAlpha              = flt(JsonKeys.PULL_BAR_ALPHA,              0.5f),
            controlTextColor          = str(JsonKeys.CONTROL_TEXT_COLOR,          "#FFFFFF"),
            controlTextAlpha          = flt(JsonKeys.CONTROL_TEXT_ALPHA,          1.0f),
            volumeTextColor           = str(JsonKeys.VOLUME_TEXT_COLOR,           "#FFFFFF"),
            volumeTextAlpha           = flt(JsonKeys.VOLUME_TEXT_ALPHA,           1.0f),
            brightnessTextColor       = str(JsonKeys.BRIGHTNESS_TEXT_COLOR,       "#FFFFFF"),
            brightnessTextAlpha       = flt(JsonKeys.BRIGHTNESS_TEXT_ALPHA,       1.0f),
            musicKnownTitleColor      = str(JsonKeys.MUSIC_KNOWN_TITLE_COLOR,     "#FFFFFF"),
            musicKnownTitleAlpha      = flt(JsonKeys.MUSIC_KNOWN_TITLE_ALPHA,     1.0f),
            musicKnownSingerColor     = str(JsonKeys.MUSIC_KNOWN_SINGER_COLOR,    "#FFFFFF"),
            musicKnownSingerAlpha     = flt(JsonKeys.MUSIC_KNOWN_SINGER_ALPHA,    1.0f),
            musicUnknownTitleColor    = str(JsonKeys.MUSIC_UNKNOWN_TITLE_COLOR,   "#FFFFFF"),
            musicUnknownTitleAlpha    = flt(JsonKeys.MUSIC_UNKNOWN_TITLE_ALPHA,   1.0f),
            musicUnknownSingerColor   = str(JsonKeys.MUSIC_UNKNOWN_SINGER_COLOR,  "#FFFFFF"),
            musicUnknownSingerAlpha   = flt(JsonKeys.MUSIC_UNKNOWN_SINGER_ALPHA,  1.0f),
            musicTimeColor            = str(JsonKeys.MUSIC_TIME_COLOR,            "#FFFFFF"),
            musicTimeAlpha            = flt(JsonKeys.MUSIC_TIME_ALPHA,            0.45f),
            musicAlbumCorner          = int(JsonKeys.MUSIC_ALBUM_CORNER,          0),
            homeControlDiyCorner      = int(JsonKeys.HOME_CONTROL_DIY_CORNER,     0),
            homeControlDiySize        = int(JsonKeys.HOME_CONTROL_DIY_SIZE,       30)
        )
    }
}
