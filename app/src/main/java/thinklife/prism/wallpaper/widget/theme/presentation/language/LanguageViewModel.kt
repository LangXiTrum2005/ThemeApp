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

package thinklife.prism.wallpaper.widget.theme.presentation.language

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPref
import thinklife.prism.wallpaper.widget.theme.data.provider.LanguageProvider
import thinklife.prism.wallpaper.widget.theme.presentation.language.model.LanguageItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val sharedPref: SharedPref,
) : ViewModel() {

    /**
     * Danh sách đã sắp xếp: ghim lên đầu → ngôn ngữ của MÁY ở vị trí thứ 4 → còn lại.
     *
     * CỐ Ý đọc `Resources.getSystem()` chứ không dùng `Locale.getDefault()`: sau khi user chọn
     * ngôn ngữ, `AppCompatDelegate` đã đổi locale của app nên `Locale.getDefault()` trả về lựa
     * chọn đó, không còn là ngôn ngữ gốc của máy — vào lại màn Language là thứ tự nhảy lung tung.
     */
    val languages: List<LanguageItem> = LanguageProvider.getLanguages(
        Resources.getSystem().configuration.locales[0]?.toLanguageTag().orEmpty(),
    )

    // Ngôn ngữ đang chọn: khởi tạo từ locale app đang áp, cập nhật khi user bấm thẻ.
    var pendingLanguage: LanguageItem = findLanguage(currentLanguageTag())
        private set

    val isNeedGotoWalkthrough: Boolean
        get() = sharedPref.isNeedGotoWalkthrough

    fun selectLanguage(language: LanguageItem) {
        pendingLanguage = language
    }

    fun saveLanguage() {
        sharedPref.setConfigLanguage(pendingLanguage.code)
    }

    /**
     * Thẻ đang áp dụng, ưu tiên locale app rồi mới tới lựa chọn đã lưu.
     *
     * Lấy THẺ ĐẦY ĐỦ chứ không chỉ `locale.language`: từ khi có biến thể vùng (en-US/en-GB,
     * pt-BR/pt-PT, ar-SA/ar-TN/ar-OM) thì `language` chỉ trả "en"/"pt"/"ar" nên không đủ để
     * biết user đang chọn dòng nào.
     */
    private fun currentLanguageTag(): String = AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()
        ?: sharedPref.getConfigLanguage()

    private fun findLanguage(languageTag: String): LanguageItem = LanguageProvider.findByTag(languageTag) ?: languages.first()
}
