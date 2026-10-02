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

import com.squareup.moshi.Moshi
import thinklife.prism.wallpaper.widget.theme.BuildConfig
import thinklife.prism.wallpaper.widget.theme.Secrets
import thinklife.prism.wallpaper.widget.theme.data.network.NetworkConstants
import thinklife.prism.wallpaper.widget.theme.data.network.api.ApiService
import thinklife.prism.wallpaper.widget.theme.data.local.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DownloadClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(NetworkConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
        sharedPref: SharedPref
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Secrets.INSTANCE.getBaseURL())
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideThemeWallpaperApiService(
        retrofit: Retrofit,
    ): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    @DownloadClient
    fun provideDownloadOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(NetworkConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.DOWNLOAD_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.DOWNLOAD_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

}
