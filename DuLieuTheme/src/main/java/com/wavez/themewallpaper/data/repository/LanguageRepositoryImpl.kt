package com.wavez.themewallpaper.data.repository

import android.content.res.Resources
import com.wavez.themewallpaper.data.model.LanguageModel
import com.wavez.themewallpaper.data.provide.LanguageProvider

class LanguageRepositoryImpl(
    private val sharedPref: SharedPref,
) : LanguageRepository {

    override fun getLanguages(): List<LanguageModel> {
        val primaryLocale = Resources.getSystem().configuration.locales[0].language
        val languages = LanguageProvider.getLanguages(primaryLocale)
        return languages
    }

    override fun saveLanguage(language: LanguageModel) {
        sharedPref.setConfigLanguage(language.code)
    }

    override fun getConfigLanguageCode() = sharedPref.getConfigLanguage()
}
