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

package thinklife.prism.wallpaper.widget.theme.data.network

object NetworkConstants {
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    // Longer timeouts for streaming file downloads
    const val DOWNLOAD_READ_TIMEOUT_SECONDS  = 120L
    const val DOWNLOAD_WRITE_TIMEOUT_SECONDS = 120L
}
