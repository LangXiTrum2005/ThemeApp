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

package thinklife.prism.wallpaper.widget.theme.di

import thinklife.prism.wallpaper.widget.theme.core.config.AppRemoteConfig
import thinklife.prism.wallpaper.widget.theme.core.config.AppRemoteConfigImpl
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RemoteConfigModule {
    @Provides
    fun provideAppRemoteConfig(
        sharedPref: SharedPref,
    ): AppRemoteConfig {
        return AppRemoteConfigImpl(
            sharedPref
        )
    }
}
