package com.wavez.themewallpaper

class Secrets private constructor() {
    external fun getBaseURL(): String

    companion object {
        @JvmField
        val INSTANCE = Secrets()

        init {
            System.loadLibrary("native-lib")
        }
    }
}
