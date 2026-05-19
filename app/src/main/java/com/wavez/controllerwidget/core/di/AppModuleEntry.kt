package com.wavez.controllerwidget.core.di

import com.wavez.controllerwidget.data.repository.LanguageRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppModuleEntry {

    fun getLanguageRepository(): LanguageRepository
}
