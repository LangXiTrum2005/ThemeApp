package com.wavez.themewallpaper.data.repository

import com.wavez.themewallpaper.data.model.PopularCategoryModel
import com.wavez.themewallpaper.data.model.icon.IconPackModel

interface IconPackRepository {

    suspend fun getIconPacks(forceRefresh: Boolean = false): Result<List<IconPackModel>>

    suspend fun getMostPopularIcons(forceRefresh: Boolean = false): Result<List<PopularCategoryModel>>

    suspend fun getIconPackDetail(id: String, forceRefresh: Boolean = false): Result<IconPackModel>
}
