package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.data.model.WallpaperCategoryModel
import com.wavez.controllerwidget.data.model.WallpaperModel

interface WallpaperRepository {

    suspend fun getCategoriesUnlimited(forceRefresh: Boolean = false): Result<List<WallpaperCategoryModel>>

    suspend fun getAllWallpapersUnlimited(forceRefresh: Boolean = false): Result<List<WallpaperModel>>
}
