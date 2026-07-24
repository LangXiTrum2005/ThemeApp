package com.wavez.themewallpaper.data.repository

import com.wavez.themewallpaper.data.model.PopularCategoryModel
import com.wavez.themewallpaper.data.model.icon.IconPackModel
import com.wavez.themewallpaper.data.network.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPackRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : IconPackRepository {

    private var cachedIconPacks: List<IconPackModel>? = null
    private var cachedMostPopularIcons: List<PopularCategoryModel>? = null

    override suspend fun getIconPacks(forceRefresh: Boolean): Result<List<IconPackModel>> {
        if (!forceRefresh) {
            cachedIconPacks?.let { return Result.success(it) }
        }
        return repositoryRequest { apiService.getIconPacks() }
            .onSuccess { cachedIconPacks = it }
    }

    override suspend fun getMostPopularIcons(forceRefresh: Boolean): Result<List<PopularCategoryModel>> {
        if (!forceRefresh) {
            cachedMostPopularIcons?.let { return Result.success(it) }
        }
        return repositoryRequest { apiService.getMostPopularIcons() }
            .onSuccess { cachedMostPopularIcons = it }
    }

    override suspend fun getIconPackDetail(
        id: String,
        forceRefresh: Boolean,
    ): Result<IconPackModel> {
        return getIconPacks(forceRefresh).mapCatching { packs ->
            packs.firstOrNull { it.id == id }
                ?: error("Icon pack not found: $id")
        }
    }
}
