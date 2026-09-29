package com.wavez.themewallpaper

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.wavez.themewallpaper.core.config.AppRemoteConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var app: MyApplication

        fun instance(): MyApplication {
            return app
        }

        fun appContext(): Context = instance().applicationContext
    }

    @Inject
    lateinit var appRemoteConfig: AppRemoteConfig

    override fun onCreate() {
        super.onCreate()
        app = this
        appRemoteConfig.fetchRemoteConfig()
    }

}
