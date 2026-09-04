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

package com.onetabb.template.android.data.provider

import com.onetabb.template.android.R
import com.onetabb.template.android.presentation.language.model.LanguageItem

/**
 * Danh sách ngôn ngữ hiển thị ở màn Language.
 *
 * `code` là thẻ BCP-47 truyền vào `AppCompatDelegate.setApplicationLocales()`. Những ngôn ngữ
 * có nhiều biến thể vùng phải dùng thẻ đầy đủ để phân biệt, nếu chỉ để mã ngôn ngữ thì 2 dòng
 * trùng mã và không chọn riêng được:
 * - English:    en-US / en-GB
 * - Português:  pt-BR / pt-PT
 * - العربية:     ar-SA / ar-TN / ar-OM
 *
 * Cờ là cờ QUỐC GIA/VÙNG của ngôn ngữ, nhiều ngôn ngữ cùng một nước thì dùng chung một cờ
 * (marathi/telugu/tamil đều dùng cờ Ấn Độ).
 *
 * TODO(project): cắt danh sách còn đúng các ngôn ngữ app thật sự dịch — mỗi dòng phải có
 * `values-<mã>` tương ứng, thừa dòng là user chọn xong app vẫn hiện tiếng Anh.
 */
object LanguageProvider {

    /** Ngôn ngữ GHIM cứng lên đầu danh sách (theo yêu cầu vận hành, không phụ thuộc máy user). */
    private val defaultTopLanguageCodes = listOf("hi", "bn", "pt-BR")

    /**
     * Vị trí chèn ngôn ngữ của MÁY (0-based) → nằm ngay sau [defaultTopLanguageCodes].
     * Mục đích: user thấy tiếng của mình gần đầu danh sách, không phải cuộn tìm.
     */
    private const val DEVICE_LANGUAGE_POSITION = 3

    val all: List<LanguageItem> = listOf(
        LanguageItem("hi", R.string.lang_hindi, R.drawable.ic_flag_hindi),
        LanguageItem("bn", R.string.lang_bangla, R.drawable.ic_flag_bangla),
        LanguageItem("pt-BR", R.string.lang_portuguese_br, R.drawable.ic_flag_pt),
        LanguageItem("en-US", R.string.lang_english_us, R.drawable.ic_flag_us),
        LanguageItem("en-GB", R.string.lang_english_uk, R.drawable.ic_flag_uk),
        LanguageItem("ar-SA", R.string.lang_arabic_sa, R.drawable.ic_flag_saudi),
        LanguageItem("ar-TN", R.string.lang_arabic_tn, R.drawable.ic_flag_tunisia),
        LanguageItem("ar-OM", R.string.lang_arabic_om, R.drawable.ic_flag_oman),
        LanguageItem("id", R.string.lang_indonesian, R.drawable.ic_flag_indonesia),
        LanguageItem("pt-PT", R.string.lang_portuguese, R.drawable.ic_flag_portuguese),
        LanguageItem("es", R.string.lang_spanish, R.drawable.ic_flag_spani),
        LanguageItem("mr", R.string.lang_marathi, R.drawable.ic_flag_hindi),
        LanguageItem("te", R.string.lang_telugu, R.drawable.ic_flag_hindi),
        LanguageItem("ta", R.string.lang_tamil, R.drawable.ic_flag_hindi),
        LanguageItem("ru", R.string.lang_russian, R.drawable.ic_flag_rusia),
        LanguageItem("fr", R.string.lang_french, R.drawable.ic_flag_french),
        LanguageItem("vi", R.string.lang_vietnamese, R.drawable.ic_flag_vi),
        LanguageItem("de", R.string.lang_german, R.drawable.ic_flag_german),
        LanguageItem("ko", R.string.lang_korean, R.drawable.ic_flag_korean),
        LanguageItem("ja", R.string.lang_japanese, R.drawable.ic_flag_japan),
        LanguageItem("tr", R.string.lang_turkish, R.drawable.ic_flag_turk),
        LanguageItem("zh", R.string.lang_chinese, R.drawable.ic_flag_china),
        LanguageItem("it", R.string.lang_italian, R.drawable.ic_flag_italia),
        LanguageItem("th", R.string.lang_thai, R.drawable.ic_flag_thai),
        LanguageItem("nl", R.string.lang_dutch, R.drawable.ic_flag_dutch),
        LanguageItem("da", R.string.lang_danish, R.drawable.ic_flag_danish),
        LanguageItem("ga", R.string.lang_irish, R.drawable.ic_flag_irish),
        LanguageItem("pl", R.string.lang_polish, R.drawable.ic_flag_polish),
        LanguageItem("zu", R.string.lang_zulu, R.drawable.ic_flag_zulu),
    )

    /**
     * Danh sách hiển thị, đã sắp xếp: **[ghim] → [ngôn ngữ của máy] → [phần còn lại]**.
     *
     * @param deviceLanguageTag thẻ locale của máy, vd `en-US`, `vi`, `pt_BR`.
     * Máy dùng ngôn ngữ app không hỗ trợ → trả về thứ tự mặc định (ghim lên đầu, còn lại giữ nguyên).
     */
    fun getLanguages(deviceLanguageTag: String): List<LanguageItem> {
        val deviceLanguage = findByTag(deviceLanguageTag) ?: return defaultOrder()

        // Phần đứng TRƯỚC ngôn ngữ của máy: ưu tiên các mã ghim, thiếu thì lấy tiếp theo thứ tự
        // khai báo. Luôn loại ngôn ngữ của máy ra để nó không bị lặp 2 lần.
        val beforeDevice = buildList {
            defaultTopLanguageCodes.forEach { code ->
                all.firstOrNull { it.matches(code) }
                    ?.takeUnless { it.hasSameCode(deviceLanguage) }
                    ?.let(::add)
            }
            all.forEach { language ->
                if (size < DEVICE_LANGUAGE_POSITION &&
                    !language.hasSameCode(deviceLanguage) &&
                    none { it.hasSameCode(language) }
                ) {
                    add(language)
                }
            }
        }.take(DEVICE_LANGUAGE_POSITION)

        return buildList {
            addAll(beforeDevice)
            add(deviceLanguage)
            all.forEach { language -> if (none { it.hasSameCode(language) }) add(language) }
        }
    }

    /**
     * Khớp một thẻ locale với danh sách: thử khớp NGUYÊN thẻ trước (`pt-BR` → Português Brasil),
     * không có mới lùi về mã ngôn ngữ gốc (`en-CA` → `en-US`… tức bản `en` đầu tiên gặp).
     * Chấp nhận cả dạng `en_US` của Java lẫn `en-US` của BCP-47.
     */
    fun findByTag(languageTag: String): LanguageItem? {
        val tag = languageTag.trim().replace('_', '-')
        if (tag.isEmpty()) return null
        return all.firstOrNull { it.matches(tag) }
            ?: tag.substringBefore('-').let { base ->
                all.firstOrNull { it.code.substringBefore('-').equals(base, ignoreCase = true) }
            }
    }

    /** Thứ tự khi không nhận diện được ngôn ngữ máy: ghim lên đầu, còn lại giữ thứ tự khai báo. */
    private fun defaultOrder(): List<LanguageItem> = buildList {
        defaultTopLanguageCodes.forEach { code ->
            all.firstOrNull { it.matches(code) }?.let(::add)
        }
        all.forEach { language -> if (none { it.hasSameCode(language) }) add(language) }
    }
}
