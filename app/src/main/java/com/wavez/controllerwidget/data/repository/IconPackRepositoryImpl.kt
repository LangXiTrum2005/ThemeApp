package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.core.di.IoDispatcher
import com.wavez.controllerwidget.core.download.DownloadState
import com.wavez.controllerwidget.core.download.IconPackDownloader
import com.wavez.controllerwidget.data.local.iconpack.IconPackFilePathProvider
import com.wavez.controllerwidget.data.local.iconpack.IconPackManifestParser
import com.wavez.controllerwidget.data.local.iconpack.IconPackStorageManager
import com.wavez.controllerwidget.data.model.IconPackModel
import com.wavez.controllerwidget.data.model.LocalIconPack
import com.wavez.controllerwidget.data.network.api.ControllerWidgetApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPackRepositoryImpl @Inject constructor(
    private val apiService: ControllerWidgetApiService,
    private val iconPackDownloader: IconPackDownloader,
    private val storageManager: IconPackStorageManager,
    private val pathProvider: IconPackFilePathProvider,
    private val manifestParser: IconPackManifestParser,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IconPackRepository {

    private var cachedIconPacks: List<IconPackModel>? = null
    private val cachedDetails = mutableMapOf<String, IconPackModel>()

    override suspend fun getIconPacks(forceRefresh: Boolean): Result<List<IconPackModel>> {
        if (!forceRefresh) {
            cachedIconPacks?.let { return Result.success(it) }
        }
        return repositoryRequest { apiService.getIconPacks() }
            .onSuccess { cachedIconPacks = it }
    }

    override suspend fun getIconPackDetail(
        id: String,
        forceRefresh: Boolean,
    ): Result<IconPackModel> {
        if (!forceRefresh) {
            cachedDetails[id]?.let { return Result.success(it) }
        }
        return repositoryRequest { apiService.getIconPackDetail(id) }
            .onSuccess { cachedDetails[id] = it }
    }

    override suspend fun isIconPackInstalled(iconPackId: String): Boolean =
        withContext(ioDispatcher) {
            pathProvider.getManifestFile(iconPackId).isFile
        }

    override suspend fun getLocalIconPack(iconPackId: String): LocalIconPack? =
        withContext(ioDispatcher) {
            val manifestFile = pathProvider.getManifestFile(iconPackId)
            if (!manifestFile.isFile) return@withContext null
            return@withContext try {
                val manifest = manifestParser.parse(manifestFile)
                LocalIconPack(
                    iconPackId = iconPackId,
                    version = manifest.version,
                    rootDir = pathProvider.getIconPackDir(iconPackId),
                    manifest = manifest,
                    manifestFile = manifestFile,
                )
            } catch (_: Throwable) {
                null
            }
        }

    override fun downloadIconPack(iconPack: IconPackModel): Flow<DownloadState<File>> = flow {
        if (isIconPackInstalled(iconPack.id)) {
            emit(DownloadState.Success(storageManager.getIconPackDir(iconPack.id)))
            return@flow
        }
        storageManager.deleteIconPack(iconPack.id)

        iconPackDownloader.download(iconPack.id, iconPack.url).collect { state ->
            when (state) {
                is DownloadState.Saving -> {
                    emit(DownloadState.Saving)
                    emit(DownloadState.Success(storageManager.getIconPackDir(iconPack.id)))
                }
                else -> emit(state)
            }
        }
    }.flowOn(ioDispatcher)

    override suspend fun deleteIconPack(iconPackId: String) {
        storageManager.deleteIconPack(iconPackId)
    }
}
