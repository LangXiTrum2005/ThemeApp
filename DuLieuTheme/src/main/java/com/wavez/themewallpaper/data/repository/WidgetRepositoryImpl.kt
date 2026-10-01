package com.wavez.themewallpaper.data.repository

import com.wavez.themewallpaper.data.local.dao.DownloadedWidgetDao
import com.wavez.themewallpaper.data.local.entity.DownloadedWidgetEntity
import com.wavez.themewallpaper.data.model.widget.WidgetCategoryModel
import com.wavez.themewallpaper.data.model.widget.WidgetsModel
import com.wavez.themewallpaper.data.network.api.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WidgetRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val downloadedWidgetDao: DownloadedWidgetDao,
) : WidgetRepository {

    private var cachedCategories: List<WidgetCategoryModel>? = null
    private var cachedWidgets: List<WidgetsModel>? = null

    override suspend fun getCategories(
        forceRefresh: Boolean,
    ): Result<List<WidgetCategoryModel>> {
        if (!forceRefresh) cachedCategories?.let { return Result.success(it) }

        return repositoryRequest { apiService.getWidgetCategories() }
            .onSuccess { cachedCategories = it }
    }

    override suspend fun getWidgetsByCategory(
        categoryId: String,
        forceRefresh: Boolean,
    ): Result<List<WidgetsModel>> = getAllWidgets(forceRefresh).map { widgets ->
        widgets.filter { it.categoryId == categoryId }
    }

    override suspend fun getAllWidgets(
        forceRefresh: Boolean,
    ): Result<List<WidgetsModel>> {
        if (!forceRefresh) cachedWidgets?.let { return Result.success(it) }

        return repositoryRequest { apiService.getAllWidgets() }
            .onSuccess { cachedWidgets = it }
    }

    override fun observeAddedWidgets(): Flow<List<WidgetsModel>> =
        downloadedWidgetDao.observeAll().map { entities -> entities.map { it.toModel() } }

    override suspend fun saveAddedWidget(widget: WidgetsModel) {
        downloadedWidgetDao.upsert(widget.toEntity())
    }

    private fun DownloadedWidgetEntity.toModel(): WidgetsModel = WidgetsModel(
        id = widgetId,
        name = name,
        thumb = thumb,
        contentS = contentS,
        contentM = contentM,
        contentL = contentL,
        premium = premium,
        categoryId = categoryId,
    )

    private fun WidgetsModel.toEntity(): DownloadedWidgetEntity = DownloadedWidgetEntity(
        widgetId = id,
        name = name,
        thumb = thumb,
        contentS = contentS,
        contentM = contentM,
        contentL = contentL,
        premium = premium,
        categoryId = categoryId,
        addedAt = System.currentTimeMillis(),
    )
}
