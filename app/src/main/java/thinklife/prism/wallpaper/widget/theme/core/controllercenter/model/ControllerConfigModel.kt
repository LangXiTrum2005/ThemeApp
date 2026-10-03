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

package thinklife.prism.wallpaper.widget.theme.core.controllercenter.model

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
    val showScreenshotButton: Boolean = true,
    val screenLockIcon: String = "",
    val screenLockSelectedIcon: String = "",
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
)
