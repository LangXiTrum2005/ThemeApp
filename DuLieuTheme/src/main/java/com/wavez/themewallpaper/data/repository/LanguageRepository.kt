package com.wavez.themewallpaper.data.repository

import com.wavez.themewallpaper.data.model.LanguageModel

interface LanguageRepository {

    fun getLanguages(): List<LanguageModel>

    fun saveLanguage(language: LanguageModel)

    fun getConfigLanguageCode(): String
}
