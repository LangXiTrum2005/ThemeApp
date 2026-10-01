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

import android.content.Context
import android.content.Intent
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.ControllerThemeAssets
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.assetUrls
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.model.ControllerConfigModel
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.service.ControlCenterService
import thinklife.prism.wallpaper.widget.theme.core.controllercenter.toConfig
import thinklife.prism.wallpaper.widget.theme.data.model.ControllerModel
import thinklife.prism.wallpaper.widget.theme.data.model.DownloadedResInfo
import thinklife.prism.wallpaper.widget.theme.data.network.api.ApiService
import thinklife.prism.wallpaper.widget.theme.di.IoDispatcher
import java.io.File
import javax.inject.Inject

class ThemeControllerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val sharedPref: SharedPref,
    moshi: Moshi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ThemeControllerRepository {

    companion object {
        private const val TAG = "ThemeControllerRepo"
        private const val BUNDLED_ASSET_FILE = "control_data.json"
        private const val CACHE_FILE = "controller_themes_cache.json"
    }

    private val listAdapter: JsonAdapter<List<ControllerModel>> = moshi.adapter(
        Types.newParameterizedType(List::class.java, ControllerModel::class.java)
    )

    private val cacheFile get() = File(context.filesDir, CACHE_FILE)

    // Danh sách lấy được từ API trong phiên này. Tách khỏi localThemes để lần gọi sau vẫn thử
    // API lại nếu lần trước phải dùng tạm bản offline.
    @Volatile
    private var remoteThemes: List<ControllerModel>? = null

    @Volatile
    private var localThemes: List<ControllerModel>? = null

    override suspend fun getControllerThemes(
        forceRefresh: Boolean
    ): Result<List<ControllerModel>> = withContext(ioDispatcher) {
        if (!forceRefresh) {
            remoteThemes?.let { return@withContext Result.success(it) }
        }

        // Server trả list rỗng cũng coi như hỏng: thà hiện bản offline còn hơn màn trống.
        val remote = repositoryRequest { apiService.getAllControllerTheme() }
            .onFailure { Log.w(TAG, "getAllControllerTheme failed, using offline data", it) }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?.sortedBy { it.rank }

        if (remote != null) {
            remoteThemes = remote
            localThemes = remote
            runCatching { cacheFile.writeText(listAdapter.toJson(remote)) }
                .onFailure { Log.w(TAG, "Cannot write controller cache", it) }
            Result.success(remote)
        } else {
            runCatching { loadLocalThemes() }
        }
    }

    override suspend fun getTheme(themeId: String): ControllerModel? = withContext(ioDispatcher) {
        findTheme(themeId)
    }

    override fun getThemeConfig(themeId: String): ControllerConfigModel? =
        findTheme(themeId)?.toConfig(context)

    override fun getActiveThemeId(): String? = sharedPref.activeThemeId

    override suspend fun isDownloaded(theme: ControllerModel): Boolean = withContext(ioDispatcher) {
        ControllerThemeAssets.isDownloaded(context, theme.id, theme.assetUrls())
    }

    override suspend fun downloadTheme(
        theme: ControllerModel,
        onProgress: (Int) -> Unit,
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val urls = theme.assetUrls()
            // Phần tử đầu là bg_all, thiếu nó overlay không vẽ được nên bắt buộc phải tải được.
            val requiredUrl = urls.firstOrNull() ?: error("Theme has no assets: ${theme.id}")
            ControllerThemeAssets.download(context, theme.id, urls, requiredUrl, onProgress)
            // Fire-and-forget stats update; failure must not block the download result
            runCatching {
                apiService.updateWallpaperDownloadAmount(DownloadedResInfo.fromController(theme))
            }.onFailure {
                Log.e(TAG, "updateWallpaperDownloadAmount failed", it)
            }
            Unit
        }
    }

    override fun applyTheme(theme: ControllerModel) {
        // Persist so service and future sessions load the correct theme
        sharedPref.activeThemeId = theme.id
        context.sendBroadcast(
            Intent(ControlCenterService.ACTION_RELOAD_THEME).setPackage(context.packageName)
        )
    }

    private fun findTheme(themeId: String): ControllerModel? =
        (remoteThemes ?: runCatching { loadLocalThemes() }.getOrNull())
            ?.firstOrNull { it.id == themeId }

    // Ưu tiên bản cache của lần gọi API thành công gần nhất, để theme đang áp dụng vẫn dựng
    // được khi offline; chưa từng gọi được API thì dùng bản đóng gói trong APK.
    private fun loadLocalThemes(): List<ControllerModel> = localThemes ?: synchronized(this) {
        localThemes ?: (readCache() ?: readBundled()).also { localThemes = it }
    }

    private fun readCache(): List<ControllerModel>? = runCatching {
        cacheFile.takeIf { it.isFile }
            ?.readText()
            ?.let(listAdapter::fromJson)
            ?.takeIf { it.isNotEmpty() }
    }.onFailure { Log.w(TAG, "Cannot read controller cache", it) }.getOrNull()

    private fun readBundled(): List<ControllerModel> =
        context.assets.open(BUNDLED_ASSET_FILE).bufferedReader().use { reader ->
            listAdapter.fromJson(reader.readText()).orEmpty().sortedBy { it.rank }
        }
}
