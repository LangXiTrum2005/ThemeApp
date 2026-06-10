package com.wavez.themewallpaper.data.model


interface ThemeRemoteItem {
    val id: String
    val categoryId : String
    val thumb: String
    val premium: Int
    val url: String
}