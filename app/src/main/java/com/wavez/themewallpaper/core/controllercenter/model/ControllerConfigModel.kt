package com.wavez.themewallpaper.core.controllercenter.model

data class ControllerConfigModel(
    val key: String = "",
    val error: String = "",

    // Network
    val airplaneIcon: String = "",
    val airplaneSelectedIcon: String = "",
    val bluetoothIcon: String = "",
    val bluetoothSelectedIcon: String = "",
    val mobileDataIcon: String = "",
    val mobileDataSelectedIcon: String = "",
    val wifiIcon: String = "",
    val wifiSelectedIcon: String = "",
    val hotspotIcon: String = "",
    val hotspotSelectedIcon: String = "",
    val syncIcon: String = "",
    val syncSelectedIcon: String = "",

    // Sound / Volume
    val soundBackground: String = "",
    val soundBigIcon: String = "",
    val soundMuteIcon: String = "",
    val soundSlider: String = "",

    // Brightness
    val lightBackground: String = "",
    val lightSlider: String = "",
    val lightIcon: String = "",
    val lightHomeIcon: String = "",
    val lightAutoBrightIcon: String = "",
    val lightAutoBrightSelectedIcon: String = "",
    val lightNightShiftIcon: String = "",
    val lightNightShiftSelectedIcon: String = "",

    // Music (basic)
    val musicNextIcon: String = "",
    val musicPauseIcon: String = "",
    val musicPlayIcon: String = "",
    val musicPreviousIcon: String = "",

    // Music (extended)
    val musicBackground: String = "",
    val albumDefaultImage: String = "",
    val musicProgressBackground: String = "",
    val musicProgressIcon: String = "",
    val musicProgressImage: String = "",
    val soundProgressBackground: String = "",
    val soundProgressIcon: String = "",
    val soundProgressImage: String = "",

    // Home backgrounds
    val homeBackground: String = "",
    val homeControlBackground: String = "",
    val homeMusicBackground: String = "",
    val controlBackground: String = "",
    val bottomDiyBackground: String = "",

    // Bottom action icons
    val batteryIcon: String = "",
    val calculatorIcon: String = "",
    val cameraIcon: String = "",
    val screenshotIcon: String = "",
    val setupIcon: String = "",
    val timingIcon: String = "",

    // Flashlight
    val flashlightIcon: String = "",
    val flashlightSelectedIcon: String = "",
    val flashlightOpenAnimation: String = "",
    val flashlightCloseAnimation: String = "",

    // Record
    val recordIcon: String = "",
    val recordSelectedIcon: String = "",
    val recordOpenAnimation: String = "",
    val recordCloseAnimation: String = "",
    val recordingIcon: String = "",

    // Focus (auto-rotate)
    val focusIcon: String = "",
    val focusSelectedIcon: String = "",
    val focusOpenAnimation: String = "",
    val focusCloseAnimation: String = "",

    // Locking (DND)
    val lockingIcon: String = "",
    val lockingSelectedIcon: String = "",
    val lockingOpenAnimation: String = "",
    val lockingCloseAnimation: String = "",

    // Ring / Mute
    val ringIcon: String = "",
    val ringSelectedIcon: String = "",
    val ringOpenAnimation: String = "",
    val ringCloseAnimation: String = "",

    val controllerJsonConfig: MyControllerJsonConfig = MyControllerJsonConfig()
) {
    companion object {
        val REQUIRED_ASSETS = listOf(
            "control_spec.json",

            "common_control_ic_airplane.png",
            "common_control_ic_airplane_select.png",
            "common_control_ic_bluetooth.png",
            "common_control_ic_bluetooth_select.png",
            "common_control_ic_data.png",
            "common_control_ic_data_select.png",
            "common_control_wifi.png",
            "common_control_wifi_select.png",
            "control_ic_hotspot.png",
            "control_ic_hotspot_select.png",
            "control_ic_sync.png",
            "control_ic_sync_select.png",
            "control_bg.png",

            "common_home_sound_bg.png",
            "common_home_sound_big.png",
            "common_home_sound_mute.png",
            "common_home_sound_slide.png",

            "common_light_bg.png",
            "common_light_slide.png",
            "light_ic_light.png",
            "home_light_icon.png",
            "light_ic_auto_bright.png",
            "light_ic_auto_bright_select.png",
            "light_ic_night_shift.png",
            "light_ic_night_shift_select.png",

            "common_music_ic_next.png",
            "common_music_ic_pause.png",
            "common_music_ic_play.png",
            "common_music_ic_previous.png",
            "music_bg.png",
            "music_im_default.png",
            "music_bg_music_progress.png",
            "music_ic_music_progress.png",
            "music_im_music_progress.png",
            "music_bg_sound_progress.png",
            "music_ic_sound_progress.png",
            "music_im_sound_progress.png",

            "home_bg.png",
            "home_control_bg.png",
            "home_music_bg.png",
            "home_bottom_diy_bg.png",

            "home_bottom_ic_battery.png",
            "home_bottom_ic_calculator.png",
            "home_bottom_ic_camera.png",
            "home_bottom_ic_screenshot.png",
            "home_bottom_ic_setup.png",
            "home_bottom_ic_timing.png",

            "home_bottom_ic_flashlight.png",
            "home_bottom_ic_flashlight_select.png",
            "home_bottom_ic_flashlight_open.pag",
            "home_bottom_ic_flashlight_close.pag",

            "home_bottom_ic_record.png",
            "home_bottom_ic_record_select.png",
            "home_bottom_ic_record_open.pag",
            "home_bottom_ic_record_close.pag",
            "home_bottom_ic_recording.png",

            "home_second_ic_focus.png",
            "home_second_ic_focus_select.png",
            "home_second_ic_focus_open.pag",
            "home_second_ic_focus_close.pag",

            "home_second_ic_locking.png",
            "home_second_ic_locking_select.png",
            "home_second_ic_locking_open.pag",
            "home_second_ic_locking_close.pag",

            "home_second_ic_ring.png",
            "home_second_ic_ring_select.png",
            "home_second_ic_ring_open.pag",
            "home_second_ic_ring_close.pag"
        )
    }
}
