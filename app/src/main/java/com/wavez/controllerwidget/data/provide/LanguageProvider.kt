package com.wavez.controllerwidget.data.provide

import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.data.model.LanguageModel

object LanguageProvider {

    const val LANGUAGE_DEFAULT_OF_DEVICE = "zzz"

    private val defaultLang = LanguageModel(
        code = LANGUAGE_DEFAULT_OF_DEVICE,
        name = "Device default",
        flag = R.drawable.ic_language,
    )

    private val enLang = LanguageModel(
        code = "en",
        name = "English",
        flag = R.drawable.flag_lan_gb,
    )

    private val frLang = LanguageModel(
        code = "fr",
        name = "Français",
        flag = R.drawable.flag_lan_fr,
    )

    private val esLang = LanguageModel(
        code = "es",
        name = "Español",
        flag = R.drawable.flag_lan_es,
    )

    private val ptLang = LanguageModel(
        code = "pt",
        name = "Português",
        flag = R.drawable.flag_lan_pt,
    )

    private val trLang = LanguageModel(
        code = "tr",
        name = "Türkçe",
        flag = R.drawable.flag_lan_tr,
    )

    private val inLang = LanguageModel(
        code = "id",
        name = "Bahasa Indonesia",
        flag = R.drawable.flag_lan_id,
    )

    private val viLang = LanguageModel(
        code = "vi",
        name = "Tiếng Việt",
        flag = R.drawable.flag_lan_vn,
    )

    private val koLang = LanguageModel(
        code = "ko",
        name = "한국어",
        flag = R.drawable.flag_lan_kr,
    )

    private val hiLang = LanguageModel(
        code = "hi",
        name = "हिन्दी",
        flag = R.drawable.flag_lan_in,
    )

    private val deLang = LanguageModel(
        code = "de",
        name = "Deutsch",
        flag = R.drawable.flag_germany,
    )

    private val nlLang = LanguageModel(
        code = "nl",
        name = "Nederlands",
        flag = R.drawable.flag_lan_nl,
    )

    private val itLang = LanguageModel(
        code = "it",
        name = "Italiano",
        flag = R.drawable.flag_lan_it,
    )

    private val zhCNLang = LanguageModel(
        code = "zh-CN",
        name = "中文",
        flag = R.drawable.flag_lan_cn,
    )

    private val caLang = LanguageModel(
        code = "en-CA",
        name = "English (Canada)",
        flag = R.drawable.flag_lan_ca,
    )

    private val availableLanguages = arrayListOf(
        enLang,
        ptLang,
        trLang,
        inLang,
        frLang,
        nlLang,
        viLang,
        caLang,
        koLang,
        hiLang,
        deLang,
        esLang,
        itLang
    )

    /**
     * Logic sắp xếp ngôn ngữ:
     * - Nếu device language có trong danh sách → lên đầu
     * - Nếu không → thêm "Device default" ở đầu
     * - English luôn hiển thị thứ hai
     * - Các ngôn ngữ còn lại sắp theo alphabet (theo name)
     */
    fun getLanguages(deviceLanguage: String): List<LanguageModel> {
        val result = mutableListOf<LanguageModel>()

        val deviceLangModel =
            availableLanguages.find { it.code.equals(deviceLanguage, ignoreCase = true) }
        if (deviceLangModel != null) {
            result.add(deviceLangModel)
        } else {
            result.add(defaultLang)
        }
        if (deviceLangModel?.code != enLang.code) {
            result.add(enLang)
        }
        val remaining = availableLanguages
            .filter { it.code != deviceLangModel?.code && it.code != enLang.code }
            .sortedBy { it.name.lowercase() }

        result.addAll(remaining)
        return result
    }

    fun getLanguageConfig(languageCode: String): LanguageModel =
        availableLanguages.firstOrNull { it.code == languageCode }
            ?: availableLanguages.first()
}
