package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.data.model.LanguageModel

interface LanguageRepository {

    fun getLanguages(): List<LanguageModel>

    fun saveLanguage(language: LanguageModel)

    fun getConfigLanguageCode(): String
}
