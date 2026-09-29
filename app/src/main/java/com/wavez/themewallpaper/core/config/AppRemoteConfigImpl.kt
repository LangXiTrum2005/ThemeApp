package com.wavez.themewallpaper.core.config

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.wavez.themewallpaper.BuildConfig
import com.wavez.themewallpaper.Secrets
import com.wavez.themewallpaper.core.config.AppRemoteConfig.Companion.SERVER_ROOT
import com.wavez.themewallpaper.data.repository.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppRemoteConfigImpl(
    private val sharedPref: SharedPref,
) : AppRemoteConfig {

    private val configDefaults: Map<String, Any> = mapOf(
        SERVER_ROOT to Secrets.INSTANCE.getBaseURL(),
    )

    override fun fetchRemoteConfig() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setDefaultsAsync(configDefaults)
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                CoroutineScope(Dispatchers.IO).launch {
                    sharedPref.severRoot = remoteConfig.getString(SERVER_ROOT)
                    Log.d(
                        "log_remote",
                        "fetchRemoteConfig: ${remoteConfig.getString(SERVER_ROOT)}"
                    )
                }
            }
        }
    }

}
