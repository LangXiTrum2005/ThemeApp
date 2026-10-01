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

package thinklife.prism.wallpaper.widget.theme.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import thinklife.prism.wallpaper.widget.theme.data.network.model.ApiResponse

typealias ControllerListResponse = ApiResponse<List<ControllerModel>>

// Mỗi field là một URL ảnh riêng lẻ (không phải file zip). control_id trên server là số,
// Moshi vẫn đọc được vào String nên không cần adapter riêng.
@JsonClass(generateAdapter = true)
data class ControllerModel(
    @param:Json(name = "control_id") val id: String,
    @param:Json(name = "control_name") val name: String = "",
    @param:Json(name = "rank") val rank: Int = 0,
    @param:Json(name = "thumbnails") val thumbnails: String,
    @param:Json(name = "flight_on") val flightOn: String = "",
    @param:Json(name = "flight_off") val flightOff: String = "",
    @param:Json(name = "airdrop_on") val airdropOn: String = "",
    @param:Json(name = "airdrop_off") val airdropOff: String = "",
    @param:Json(name = "bluetooth_on") val bluetoothOn: String = "",
    @param:Json(name = "bluetooth_off") val bluetoothOff: String = "",
    @param:Json(name = "wifi_on") val wifiOn: String = "",
    @param:Json(name = "wifi_off") val wifiOff: String = "",
    @param:Json(name = "rotate_on") val rotateOn: String = "",
    @param:Json(name = "rotate_off") val rotateOff: String = "",
    @param:Json(name = "mute_on") val muteOn: String = "",
    @param:Json(name = "mute_off") val muteOff: String = "",
    @param:Json(name = "flash_on") val flashOn: String = "",
    @param:Json(name = "flash_off") val flashOff: String = "",
    @param:Json(name = "battery_off") val batteryOff: String = "",
    @param:Json(name = "record_on") val recordOn: String = "",
    @param:Json(name = "record_off") val recordOff: String = "",
    @param:Json(name = "setting_off") val settingOff: String = "",
    @param:Json(name = "lock_on") val lockOn: String = "",
    @param:Json(name = "lock_off") val lockOff: String = "",
    @param:Json(name = "time_off") val timeOff: String = "",
    @param:Json(name = "calculator_off") val calculatorOff: String = "",
    @param:Json(name = "camera_off") val cameraOff: String = "",
    @param:Json(name = "sound") val sound: String = "",
    @param:Json(name = "mute") val mute: String = "",
    @param:Json(name = "light") val light: String = "",
    @param:Json(name = "dark") val dark: String = "",
    @param:Json(name = "back") val back: String = "",
    @param:Json(name = "next") val next: String = "",
    @param:Json(name = "play") val play: String = "",
    @param:Json(name = "pause") val pause: String = "",
    @param:Json(name = "bg_function") val bgFunction: String = "",
    @param:Json(name = "bg_music") val bgMusic: String = "",
    @param:Json(name = "bg_time") val bgTime: String = "",
    @param:Json(name = "bg_light") val bgLight: String = "",
    @param:Json(name = "progress_light") val progressLight: String = "",
    @param:Json(name = "bg_volume") val bgVolume: String = "",
    @param:Json(name = "progress_volume") val progressVolume: String = "",
    @param:Json(name = "bg_all") val bgAll: String = "",
    @param:Json(name = "music_font_color") val musicFontColor: String = "",
    @param:Json(name = "screentime_font_color") val screentimeFontColor: String = "",
)
