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

sealed class DownloadState<out T> {
    data object Idle : DownloadState<Nothing>()

    // percent in 0..100, or -1 when content-length is unknown
    data class Downloading(val percent: Int) : DownloadState<Nothing>()

    // controller theme only: zip is saved, extraction in progress
    data object Extracting : DownloadState<Nothing>()

    // file is on disk, Room record is about to be written
    data object Saving : DownloadState<Nothing>()

    data class Success<out T>(val file: T) : DownloadState<T>()
    data class Failure(val error: DownloadError) : DownloadState<Nothing>()
}
