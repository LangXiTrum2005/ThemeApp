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

package thinklife.prism.wallpaper.widget.theme.core.download

sealed class DownloadError(val cause: Throwable? = null) {
    data class NetworkError(val exception: Throwable) : DownloadError(exception)
    data class FileWriteError(val exception: Throwable) : DownloadError(exception)
    data object EmptyFile : DownloadError()
    data object InvalidZip : DownloadError()
    data class ExtractError(val exception: Throwable) : DownloadError(exception)
    data class MoveError(val exception: Throwable) : DownloadError(exception)
    data class UnknownError(val exception: Throwable) : DownloadError(exception)
}
