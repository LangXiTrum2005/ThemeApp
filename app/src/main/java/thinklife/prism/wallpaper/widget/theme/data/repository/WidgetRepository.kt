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

import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetCategoryModel
import thinklife.prism.wallpaper.widget.theme.data.model.widget.WidgetsModel
import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    suspend fun getCategories(forceRefresh: Boolean = false): Result<List<WidgetCategoryModel>>

    suspend fun getAllWidgets(forceRefresh: Boolean = false): Result<List<WidgetsModel>>

    suspend fun getWidgetsByCategory(
        categoryId: String,
        forceRefresh: Boolean = false,
    ): Result<List<WidgetsModel>>

    /** Widgets the user has added to the home screen, newest first. */
    fun observeAddedWidgets(): Flow<List<WidgetsModel>>

    suspend fun saveAddedWidget(widget: WidgetsModel)
}
