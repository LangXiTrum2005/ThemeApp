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

package thinklife.prism.wallpaper.widget.theme.data.provider

import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.data.model.LanguageModel

object ThemeLanguageProvider {


    const val LANGUAGE_DEFAULT_OF_DEVICE = "zzz"

    val localeTranslateDefaults = listOf(
        "af",
        "am",
        "az",
        "be",
        "bn",
        "bn_rBD",
        "ca",
        "ceb",
        "cs",
        "et",
        "fa",
        "gl",
        "gu",
        "ha",
        "ga",
        "hr",
        "hu",
        "ig",
        "in",
        "jv",
        "kk",
        "km",
        "kn",
        "ml",
        "mr",
        "ms",
        "my",
        "ne",
        "nl",
        "pa",
        "ps",
        "ro",
        "sd",
        "si",
        "sk",
        "sl",
        "sr",
        "su",
        "ta",
        "te",
        "tg",
        "uk",
        "ur",
        "uz",
        "yo",
        "ar",
        "da",
        "de",
        "el",
        "en",
        "es",
        "es_rES",
        "es_rUS",
        "ru",
        "it",
        "ja",
        "fi",
        "tl",
        "fr",
        "ko",
        "fr_rCA",
        "hi",
        "no",
        "vi",
        "pt",
        "pt_rBR",
        "pl",
        "sv",
        "th",
        "tr",
        "zh",
        "zh_rCN",
        "zh_rHK",
        "zh_rTW"
    )

    private val defaultLang = LanguageModel(
        code = LANGUAGE_DEFAULT_OF_DEVICE,
        name = "Default",
        englishName = "System Default",
        flag = R.drawable.ic_language
    )

    private val enLang = LanguageModel(
        code = "en",
        name = "English",
        englishName = "English",
        flag = R.drawable.flag_lan_gb
    )

    private val frLang = LanguageModel(
        code = "fr",
        name = "Français",
        englishName = "French",
        flag = R.drawable.flag_lan_fr
    )

    private val esLang = LanguageModel(
        code = "es",
        name = "Español",
        englishName = "Spanish",
        flag = R.drawable.flag_lan_es
    )

    private val ptLang = LanguageModel(
        code = "pt",
        name = "Português",
        englishName = "Portuguese",
        flag = R.drawable.flag_lan_pt
    )

    private val inLang = LanguageModel(
        code = "id", // sửa lại đúng ISO code
        name = "Bahasa Indonesia",
        englishName = "Indonesian",
        flag = R.drawable.flag_lan_id
    )

    private val zhCNLang = LanguageModel(
        code = "zh-CN",
        name = "中文",
        englishName = "Chinese (Simplified)",
        flag = R.drawable.flag_lan_cn
    )

    private val hiLang = LanguageModel(
        code = "hi",
        name = "हिन्दी",
        englishName = "Hindi",
        flag = R.drawable.flag_lan_in
    )

    private val deLang = LanguageModel(
        code = "de",
        name = "Deutsch",
        englishName = "German",
        flag = R.drawable.flag_germany
    )

    private val viLang = LanguageModel(
        code = "vi",
        name = "Tiếng Việt",
        englishName = "Vietnamese",
        flag = R.drawable.flag_lan_vn
    )

    private val itLang = LanguageModel(
        code = "it",
        name = "Italiano",
        englishName = "Italian",
        flag = R.drawable.flag_lan_it
    )

    private val availableLanguages = arrayListOf(
        enLang,
        esLang,
        deLang,
        ptLang,
        inLang,
        hiLang,
        viLang,
        frLang,
        zhCNLang,
        itLang,
    )

    fun getLanguages(deviceLanguage: String): List<LanguageModel> {
        val deviceLanguageInList = availableLanguages.find { it.code == deviceLanguage }
        val sortedList = availableLanguages.toMutableList()
        if (deviceLanguageInList != null) {
            sortedList.remove(deviceLanguageInList)
            sortedList.add(0, deviceLanguageInList)
        } else {
            val deviceLanguageTranslate = localeTranslateDefaults.find { it == deviceLanguage }
            if (deviceLanguageTranslate != null) {
                sortedList.add(0, defaultLang)
            }
        }
        return sortedList
    }


    fun getLanguageConfig(languageCode: String): LanguageModel {
        return availableLanguages.firstOrNull { it.code == languageCode }
            ?: availableLanguages.first()
    }
}