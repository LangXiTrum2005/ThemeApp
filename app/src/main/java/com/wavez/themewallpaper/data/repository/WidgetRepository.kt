package com.wavez.themewallpaper.data.repository

import com.wavez.themewallpaper.data.model.widget.WidgetCategoryModel
import com.wavez.themewallpaper.data.model.widget.WidgetsModel
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
