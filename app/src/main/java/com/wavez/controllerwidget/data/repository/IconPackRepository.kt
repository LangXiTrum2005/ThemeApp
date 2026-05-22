package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.data.model.icon.IconPackModel

interface IconPackRepository {

    suspend fun getIconPacks(forceRefresh: Boolean = false): Result<List<IconPackModel>>

    suspend fun getIconPackDetail(id: String, forceRefresh: Boolean = false): Result<IconPackModel>
}
