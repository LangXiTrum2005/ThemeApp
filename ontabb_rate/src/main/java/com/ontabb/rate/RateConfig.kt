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

package com.ontabb.rate

import androidx.annotation.FontRes

/** Kiểu hiển thị của khối đánh giá. Nội dung và luật xử lý số sao y hệt nhau, chỉ khác khung chứa. */
enum class RateStyle {
    /** Hộp thoại nổi giữa màn hình. */
    DIALOG,

    /** Bottom sheet trượt từ mép dưới, tràn hết bề ngang. */
    BOTTOM_SHEET,
}

/**
 * Cấu hình của module đánh giá. Truyền một lần ở `Application.onCreate` qua [OneTabbRate.init].
 *
 * @param contactEmail hòm thư nhận góp ý khi user chấm 1-3 sao. Bắt buộc.
 * @param storeId package hiển thị trên Play Store. Để `null` thì lấy `packageName` của app -
 * chỉ cần truyền khi app có `applicationIdSuffix` (bản dev/internal) mà vẫn muốn trỏ về listing
 * của bản chính thức.
 * @param style kiểu hiển thị, xem [RateStyle]. Đổi giá trị này là đổi cả hai điểm kích hoạt
 * ([OneTabbRate.showOnHome] và [OneTabbRate.showOnDemand]) — không có chuyện mỗi chỗ một kiểu.
 * @param isEnabled công tắc tổng cho các điểm kích hoạt TỰ ĐỘNG ([OneTabbRate.showOnHome] và
 * [OneTabbRate.showOnBackPressed]). Tắt thì bộ đếm cũng đứng yên, bật lại user không bị nhảy cóc
 * qua mấy lượt. KHÔNG ảnh hưởng [OneTabbRate.showOnDemand] — user tự bấm "Rate us" thì vẫn bung.
 * @param secondAppOpenAt bung hộp thoại ở lần mở app thứ mấy. `0` để tắt điểm kích hoạt này.
 * @param showOnHomeAt các lần quay lại màn Home được phép bung. Rỗng để tắt.
 * @param showOnBackAt các lần user bấm nút back của MÁY để thoát app được phép bung. Rỗng để
 * tắt. Cả hai danh sách này thường do Remote Config đẩy xuống — xem [OneTabbRate.updateShowRules].
 * @param fontRegular / fontBold font của app nhúng module, để hộp thoại khớp typography chung.
 * Bỏ trống thì dùng font mặc định của hệ thống.
 */
data class RateConfig(
    val contactEmail: String,
    val storeId: String? = null,
    val style: RateStyle = RateStyle.DIALOG,
    val isEnabled: Boolean = true,
    val secondAppOpenAt: Int = 2,
    val showOnHomeAt: Set<Int> = setOf(2, 4, 6),
    val showOnBackAt: Set<Int> = setOf(1, 3),
    @param:FontRes val fontRegular: Int? = null,
    @param:FontRes val fontBold: Int? = null,
)
