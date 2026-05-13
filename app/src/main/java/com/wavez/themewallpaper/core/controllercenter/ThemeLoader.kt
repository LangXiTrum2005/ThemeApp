package com.wavez.themewallpaper.core.controllercenter

import android.content.Context
import com.wavez.themewallpaper.core.controllercenter.model.ControllerConfigModel
import com.wavez.themewallpaper.core.controllercenter.model.MyControllerJsonConfig
import java.io.File

object ThemeLoader {

    private const val THEME_BASE_DIR = "theme"

    fun isReady(context: Context, themeName: String = "theme_1"): Boolean {
        val dir = File(context.filesDir, "$THEME_BASE_DIR/$themeName")
        return ControllerConfigModel.REQUIRED_ASSETS.all { File(dir, it).exists() }
    }

    fun missingAssets(context: Context, themeName: String = "theme_1"): List<String> {
        val dir = File(context.filesDir, "$THEME_BASE_DIR/$themeName")
        return ControllerConfigModel.REQUIRED_ASSETS.filter { !File(dir, it).exists() }
    }

    fun load(context: Context, themeName: String = "theme_1"): ControllerConfigModel {
        val dir = File(context.filesDir, "$THEME_BASE_DIR/$themeName")
        val missing = ControllerConfigModel.REQUIRED_ASSETS.filter { !File(dir, it).exists() }
        if (missing.isNotEmpty()) {
            android.util.Log.w("ThemeLoader", "Missing ${missing.size} assets: ${missing.take(3)}...")
        }
        fun p(name: String) = File(dir, name).absolutePath

        val jsonConfig = loadJsonConfig(File(dir, "control_spec.json"))

        return ControllerConfigModel(
            key = themeName,

            // Network
            airplaneIcon = p("common_control_ic_airplane.png"),
            airplaneSelectedIcon = p("common_control_ic_airplane_select.png"),
            bluetoothIcon = p("common_control_ic_bluetooth.png"),
            bluetoothSelectedIcon = p("common_control_ic_bluetooth_select.png"),
            mobileDataIcon = p("common_control_ic_data.png"),
            mobileDataSelectedIcon = p("common_control_ic_data_select.png"),
            wifiIcon = p("common_control_wifi.png"),
            wifiSelectedIcon = p("common_control_wifi_select.png"),
            hotspotIcon = p("control_ic_hotspot.png"),
            hotspotSelectedIcon = p("control_ic_hotspot_select.png"),
            syncIcon = p("control_ic_sync.png"),
            syncSelectedIcon = p("control_ic_sync_select.png"),
            controlBackground = p("control_bg.png"),

            // Sound / Volume
            soundBackground = p("common_home_sound_bg.png"),
            soundBigIcon = p("common_home_sound_big.png"),
            soundMuteIcon = p("common_home_sound_mute.png"),
            soundSlider = p("common_home_sound_slide.png"),

            // Brightness
            lightBackground = p("common_light_bg.png"),
            lightSlider = p("common_light_slide.png"),
            lightIcon = p("light_ic_light.png"),
            lightHomeIcon = p("home_light_icon.png"),
            lightAutoBrightIcon = p("light_ic_auto_bright.png"),
            lightAutoBrightSelectedIcon = p("light_ic_auto_bright_select.png"),
            lightNightShiftIcon = p("light_ic_night_shift.png"),
            lightNightShiftSelectedIcon = p("light_ic_night_shift_select.png"),

            // Music basic
            musicNextIcon = p("common_music_ic_next.png"),
            musicPauseIcon = p("common_music_ic_pause.png"),
            musicPlayIcon = p("common_music_ic_play.png"),
            musicPreviousIcon = p("common_music_ic_previous.png"),

            // Music extended
            musicBackground = p("music_bg.png"),
            albumDefaultImage = p("music_im_default.png"),
            musicProgressBackground = p("music_bg_music_progress.png"),
            musicProgressIcon = p("music_ic_music_progress.png"),
            musicProgressImage = p("music_im_music_progress.png"),
            soundProgressBackground = p("music_bg_sound_progress.png"),
            soundProgressIcon = p("music_ic_sound_progress.png"),
            soundProgressImage = p("music_im_sound_progress.png"),

            // Home backgrounds
            homeBackground = p("home_bg.jpg"),
            homeControlBackground = p("home_control_bg.png"),
            homeMusicBackground = p("home_music_bg.png"),
            bottomDiyBackground = p("home_bottom_diy_bg.png"),

            // Bottom action icons
            batteryIcon = p("home_bottom_ic_battery.png"),
            calculatorIcon = p("home_bottom_ic_calculator.png"),
            cameraIcon = p("home_bottom_ic_camera.png"),
            screenshotIcon = p("home_bottom_ic_screenshot.png"),
            setupIcon = p("home_bottom_ic_setup.png"),
            timingIcon = p("home_bottom_ic_timing.png"),

            // Flashlight
            flashlightIcon = p("home_bottom_ic_flashlight.png"),
            flashlightSelectedIcon = p("home_bottom_ic_flashlight_select.png"),
            flashlightOpenAnimation = p("home_bottom_ic_flashlight_open.pag"),
            flashlightCloseAnimation = p("home_bottom_ic_flashlight_close.pag"),

            // Record
            recordIcon = p("home_bottom_ic_record.png"),
            recordSelectedIcon = p("home_bottom_ic_record_select.png"),
            recordOpenAnimation = p("home_bottom_ic_record_open.pag"),
            recordCloseAnimation = p("home_bottom_ic_record_close.pag"),
            recordingIcon = p("home_bottom_ic_recording.png"),

            // Focus (auto-rotate)
            focusIcon = p("home_second_ic_focus.png"),
            focusSelectedIcon = p("home_second_ic_focus_select.png"),
            focusOpenAnimation = p("home_second_ic_focus_open.pag"),
            focusCloseAnimation = p("home_second_ic_focus_close.pag"),

            // Locking (DND)
            lockingIcon = p("home_second_ic_locking.png"),
            lockingSelectedIcon = p("home_second_ic_locking_select.png"),
            lockingOpenAnimation = p("home_second_ic_locking_open.pag"),
            lockingCloseAnimation = p("home_second_ic_locking_close.pag"),

            // Ring / Mute
            ringIcon = p("home_second_ic_ring.png"),
            ringSelectedIcon = p("home_second_ic_ring_select.png"),
            ringOpenAnimation = p("home_second_ic_ring_open.pag"),
            ringCloseAnimation = p("home_second_ic_ring_close.pag"),

            controllerJsonConfig = jsonConfig
        )
    }

    private fun loadJsonConfig(file: File): MyControllerJsonConfig {
        if (!file.exists()) return MyControllerJsonConfig()
        return try {
            parseJsonConfig(file.readText())
        } catch (_: Exception) {
            MyControllerJsonConfig()
        }
    }

    private fun parseJsonConfig(json: String): MyControllerJsonConfig {
        fun str(key: String, default: String): String =
            Regex("\"$key\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: default
        fun flt(key: String, default: Float): Float =
            Regex("\"$key\"\\s*:\\s*([0-9.]+)").find(json)?.groupValues?.get(1)?.toFloatOrNull() ?: default
        fun int(key: String, default: Int): Int =
            Regex("\"$key\"\\s*:\\s*([0-9]+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: default

        return MyControllerJsonConfig(
            pullBarColor = str("pullBarColor", "#000000"),
            pullBarAlpha = flt("pullBarAlpha", 0.5f),
            controlTextColor = str("controlTextColor", "#FFFFFF"),
            controlTextAlpha = flt("controlTextAlpha", 1.0f),
            volumeTextColor = str("volumeTextColor", "#FFFFFF"),
            volumeTextAlpha = flt("volumeTextAlpha", 1.0f),
            brightnessTextColor = str("brightnessTextColor", "#FFFFFF"),
            brightnessTextAlpha = flt("brightnessTextAlpha", 1.0f),
            musicKnownTitleColor = str("musicKnownTitleColor", "#FFFFFF"),
            musicKnownTitleAlpha = flt("musicKnownTitleAlpha", 1.0f),
            musicKnownSingerColor = str("musicKnownSingerColor", "#FFFFFF"),
            musicKnownSingerAlpha = flt("musicKnownSingerAlpha", 1.0f),
            musicUnknownTitleColor = str("musicUnknownTitleColor", "#FFFFFF"),
            musicUnknownTitleAlpha = flt("musicUnknownTitleAlpha", 1.0f),
            musicUnknownSingerColor = str("musicUnknownSingerColor", "#FFFFFF"),
            musicUnknownSingerAlpha = flt("musicUnknownSingerAlpha", 1.0f),
            musicTimeColor = str("musicTimeColor", "#FFFFFF"),
            musicTimeAlpha = flt("musicTimeAlpha", 0.45f),
            musicAlbumCorner = int("musicAlbumCorner", 0),
            homeControlDiyCorner = int("homeControlDiyCorner", 0),
            homeControlDiySize = int("homeControlDiySize", 30)
        )
    }
}
