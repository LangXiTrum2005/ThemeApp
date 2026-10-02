/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.core.config

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import thinklife.prism.wallpaper.widget.theme.BuildConfig
import thinklife.prism.wallpaper.widget.theme.Secrets
import thinklife.prism.wallpaper.widget.theme.core.config.AppRemoteConfig.Companion.SERVER_ROOT
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPref
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
