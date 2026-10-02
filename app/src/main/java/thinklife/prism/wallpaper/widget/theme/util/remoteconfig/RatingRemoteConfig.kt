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

package thinklife.prism.wallpaper.widget.theme.util.remoteconfig

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ontabb.rate.OneTabbRate
import org.json.JSONArray
import org.json.JSONObject

/**
 * Điều khiển hộp thoại đánh giá bằng Firebase Remote Config. Một tham số duy nhất, giá trị là
 * chuỗi JSON:
 *
 * ```
 * Key:   rating_show
 * Value: {"enable":true,"home_show_rating":[2,4,6],"back_device_rating":[1,3,5]}
 * ```
 *
 * - `enable`             — công tắc tổng cho các điểm bung TỰ ĐỘNG. Nút "Rate us" trong Settings
 *                          nằm NGOÀI công tắc, tắt rồi bấm vẫn bung.
 * - `home_show_rating`   — bung ở lần user vào / quay lại tab Home thứ mấy.
 * - `back_device_rating` — bung ở lần user bấm back của máy để thoát app thứ mấy.
 *
 * Console KHÔNG kiểm cú pháp JSON của tham số String: gõ hỏng thì cả ba khoá bị bỏ qua và app
 * chạy tiếp bằng giá trị đang có, kể cả khoá `enable`. Dán qua một trình kiểm JSON trước khi
 * publish.
 *
 * - `home_show_rating`  — bung ở lần user vào / quay lại tab Home thứ mấy.
 * - `back_device_rating` — bung ở lần user bấm back của máy để thoát app thứ mấy.
 *
 * Danh sách rỗng (`[]`) là TẮT hẳn điểm kích hoạt đó. Thiếu khoá nào thì khoá đó giữ nguyên giá
 * trị đang chạy, nên AdOps đẩy nửa cấu hình cũng không làm mất nửa còn lại.
 *
 * [apply] đọc giá trị ĐÃ activate, không tự fetch: cold start ở Splash đã fetch + activate một
 * lần cho cả app (xem `AdsBootstrap.awaitColdStart`). Gọi thêm một lần nữa ở
 * `Application.onCreate` để phiên hiện tại dùng ngay giá trị cache của phiên trước, thay vì chạy
 * mặc định trong code tới tận lúc fetch xong.
 */
object RatingRemoteConfig {

    const val KEY_RATING_SHOW = "rating_show"

    private const val TAG = "RatingRemoteConfig"
    private const val FIELD_ENABLE = "enable"
    private const val FIELD_HOME = "home_show_rating"
    private const val FIELD_BACK = "back_device_rating"

    /**
     * Hai danh sách lần-bung đọc được từ Remote Config.
     *
     * `null` = tham số KHÔNG khai khoá đó, giữ nguyên giá trị đang chạy. Khác hẳn tập rỗng —
     * tập rỗng là AdOps cố ý tắt điểm kích hoạt.
     */
    data class ShowRules(
        val isEnabled: Boolean?,
        val homeAt: Set<Int>?,
        val backAt: Set<Int>?,
    )

    /** Đọc tham số và bơm vào [OneTabbRate]. Hỏng hay thiếu thì giữ nguyên cấu hình cũ. */
    fun apply() {
        val raw = runCatching { FirebaseRemoteConfig.getInstance().getString(KEY_RATING_SHOW) }
            .getOrNull()
            ?: return
        val rules = parse(raw) ?: return
        rules.isEnabled?.let(OneTabbRate::setEnabled)
        OneTabbRate.updateShowRules(showOnHomeAt = rules.homeAt, showOnBackAt = rules.backAt)
    }

    /**
     * Tách khỏi [apply] để test được mà không cần Firebase. Trả `null` khi chuỗi rỗng hoặc không
     * phải JSON hợp lệ — cấu hình hỏng thì app chạy tiếp bằng giá trị cũ, không crash.
     */
    internal fun parse(raw: String): ShowRules? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || trimmed == "{}") return null
        val json = runCatching { JSONObject(trimmed) }.getOrElse {
            Log.w(TAG, "'$KEY_RATING_SHOW' không phải JSON hợp lệ, bỏ qua: $trimmed", it)
            return null
        }
        return ShowRules(
            // Thiếu khoá `enable` = giữ nguyên giá trị đang chạy, KHÔNG mặc định thành false.
            isEnabled = if (json.has(FIELD_ENABLE)) json.optBoolean(FIELD_ENABLE, true) else null,
            homeAt = json.readCounts(FIELD_HOME),
            backAt = json.readCounts(FIELD_BACK),
        )
    }

    /** Đọc một mảng số nguyên; bỏ 0 và số âm vì bộ đếm bắt đầu từ 1. */
    private fun JSONObject.readCounts(field: String): Set<Int>? {
        val array: JSONArray = optJSONArray(field) ?: return null
        return buildSet {
            for (i in 0 until array.length()) {
                val value = array.optInt(i, 0)
                if (value > 0) add(value)
            }
        }
    }
}
