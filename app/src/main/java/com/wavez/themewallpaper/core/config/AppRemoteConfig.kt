package com.wavez.themewallpaper.core.config

interface AppRemoteConfig {

    companion object {

        const val SERVER_ROOT = "server_root"

    }

    fun fetchRemoteConfig()

}
