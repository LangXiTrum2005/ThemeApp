package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.data.model.IconPackModel
import com.wavez.controllerwidget.data.model.LocalIconPack
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IconPackRepository {

    suspend fun getIconPacks(forceRefresh: Boolean = false): Result<List<IconPackModel>>

    suspend fun getIconPackDetail(id: String, forceRefresh: Boolean = false): Result<IconPackModel>

    /**
     * Returns true if the icon pack is fully installed on disk (manifest.json exists).
     */
    suspend fun isIconPackInstalled(iconPackId: String): Boolean

    suspend fun getLocalIconPack(iconPackId: String): LocalIconPack?

    fun downloadIconPack(iconPack: IconPackModel): Flow<DownloadState<File>>

    suspend fun deleteIconPack(iconPackId: String)
}
