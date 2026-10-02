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

import thinklife.prism.wallpaper.widget.theme.data.network.model.ApiResponse

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
