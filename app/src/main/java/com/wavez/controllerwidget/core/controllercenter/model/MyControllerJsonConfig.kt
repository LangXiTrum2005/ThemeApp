package com.wavez.controllerwidget.core.controllercenter.model

data class MyControllerJsonConfig(
    val pullBarColor: String = "#000000",
    val pullBarAlpha: Float = 0.5f,

    val controlTextColor: String = "#FFFFFF",
    val controlTextAlpha: Float = 1.0f,

    val volumeTextColor: String = "#FFFFFF",
    val volumeTextAlpha: Float = 1.0f,

    val brightnessTextColor: String = "#FFFFFF",
    val brightnessTextAlpha: Float = 1.0f,

    val musicKnownTitleColor: String = "#FFFFFF",
    val musicKnownTitleAlpha: Float = 1.0f,

    val musicKnownSingerColor: String = "#FFFFFF",
    val musicKnownSingerAlpha: Float = 1.0f,

    val musicUnknownTitleColor: String = "#FFFFFF",
    val musicUnknownTitleAlpha: Float = 1.0f,

    val musicUnknownSingerColor: String = "#FFFFFF",
    val musicUnknownSingerAlpha: Float = 1.0f,

    val musicTimeColor: String = "#FFFFFF",
    val musicTimeAlpha: Float = 0.45f,

    val musicAlbumCorner: Int = 0,
    val homeControlDiyCorner: Int = 0,
    val homeControlDiySize: Int = 30
)
