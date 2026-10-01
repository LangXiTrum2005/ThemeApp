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

package thinklife.prism.wallpaper.widget.theme.data.repository

import thinklife.prism.wallpaper.widget.theme.data.model.PopularCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconPackModel
import thinklife.prism.wallpaper.widget.theme.data.network.api.ApiService
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
