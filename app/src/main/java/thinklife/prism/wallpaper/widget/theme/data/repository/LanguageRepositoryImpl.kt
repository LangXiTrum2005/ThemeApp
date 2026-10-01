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

package thinklife.prism.wallpaper.widget.theme.data.repository

import android.content.res.Resources
import thinklife.prism.wallpaper.widget.theme.data.model.LanguageModel
import thinklife.prism.wallpaper.widget.theme.data.provider.ThemeLanguageProvider

class LanguageRepositoryImpl(
    private val sharedPref: SharedPref,
) : LanguageRepository {

    override fun getLanguages(): List<LanguageModel> {
        val primaryLocale = Resources.getSystem().configuration.locales[0].language
        val languages = ThemeLanguageProvider.getLanguages(primaryLocale)
        return languages
    }

    override fun saveLanguage(language: LanguageModel) {
        sharedPref.setConfigLanguage(language.code)
    }

    override fun getConfigLanguageCode() = sharedPref.getConfigLanguage()
}
