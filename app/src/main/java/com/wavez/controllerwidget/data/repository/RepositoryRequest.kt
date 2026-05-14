package com.wavez.controllerwidget.data.repository

import com.wavez.controllerwidget.data.network.model.ApiResponse

suspend fun <T> repositoryRequest(
    block: suspend () -> ApiResponse<T>
): Result<T> {
    return runCatching {
        val response = block()
        if (!response.isSuccess) {
            error(response.message.ifBlank { "Request failed with status ${response.status}" })
        }
        response.data ?: error(response.message.ifBlank { "Response data is null" })
    }
}
