package com.wavez.themewallpaper.di

import com.wavez.themewallpaper.data.repository.LanguageRepository
import com.wavez.themewallpaper.data.repository.SharedPref
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppModuleEntry {

    fun getLanguageRepository(): LanguageRepository

    fun getSharedPref(): SharedPref
}
