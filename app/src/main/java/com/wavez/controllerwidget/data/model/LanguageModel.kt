package com.wavez.controllerwidget.data.model

import androidx.annotation.DrawableRes

class LanguageModel(
    val code: String,
    val name: String,
    val englishName : String,
    @DrawableRes val flag: Int
)