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

package thinklife.prism.wallpaper.widget.theme.presentation.iconpack.model

import thinklife.prism.wallpaper.widget.theme.data.model.icon.IconPackModel

enum class SetIconState { APPLIED, INSTALL, CHOOSE }

data class IconPackDetailState(
    val pack: IconPackModel,
    val items: List<IconPackItemUi>,
)
