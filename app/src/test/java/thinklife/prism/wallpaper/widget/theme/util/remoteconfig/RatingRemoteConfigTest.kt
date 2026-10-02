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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tham số `rating_show` do AdOps gõ tay trên Firebase Console, nên phần đọc phải chịu được cả
 * chuỗi hỏng: sai cú pháp hay thiếu khoá thì app giữ nguyên cấu hình đang chạy, không được crash.
 */
class RatingRemoteConfigTest {

    @Test
    fun `doc du cong tac va hai danh sach`() {
        val rules = RatingRemoteConfig.parse(
            """{"enable":true,"home_show_rating":[2,4,6],"back_device_rating":[1,3,5]}""",
        )
        assertEquals(true, rules?.isEnabled)
        assertEquals(setOf(2, 4, 6), rules?.homeAt)
        assertEquals(setOf(1, 3, 5), rules?.backAt)
    }

    @Test
    fun `doc cong tac enable`() {
        assertEquals(false, RatingRemoteConfig.parse("""{"enable":false}""")?.isEnabled)
        assertEquals(true, RatingRemoteConfig.parse("""{"enable":true}""")?.isEnabled)
    }

    @Test
    fun `thieu khoa thi giu nguyen gia tri dang chay`() {
        val rules = RatingRemoteConfig.parse("""{"home_show_rating":[1]}""")
        assertEquals(setOf(1), rules?.homeAt)
        assertNull("thiếu khoá phải trả null chứ không phải tập rỗng", rules?.backAt)
    }

    @Test
    fun `mang rong la tat han diem kich hoat`() {
        val rules = RatingRemoteConfig.parse("""{"back_device_rating":[]}""")
        assertEquals(emptySet<Int>(), rules?.backAt)
    }

    @Test
    fun `bo qua so khong va so am`() {
        val rules = RatingRemoteConfig.parse("""{"home_show_rating":[0,-3,2,2,5]}""")
        assertEquals(setOf(2, 5), rules?.homeAt)
    }

    @Test
    fun `thieu khoa enable thi giu nguyen gia tri dang chay`() {
        val rules = RatingRemoteConfig.parse("""{"home_show_rating":[2]}""")
        assertNull("thiếu enable phải trả null, KHÔNG được coi là tắt", rules?.isEnabled)
    }

    @Test
    fun `chuoi hong hoac rong tra null`() {
        assertNull(RatingRemoteConfig.parse(""))
        assertNull(RatingRemoteConfig.parse("{}"))
        assertNull(RatingRemoteConfig.parse("không phải json"))
        assertNull(RatingRemoteConfig.parse("""{"home_show_rating":[1,2"""))
    }
}
