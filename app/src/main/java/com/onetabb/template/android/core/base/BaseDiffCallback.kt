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

package com.onetabb.template.android.core.base

import androidx.recyclerview.widget.DiffUtil

class BaseDiffCallback<T : Any>(
    private val areItemsTheSameCallback: (oldItem: T, newItem: T) -> Boolean,
    private val areContentsTheSameCallback: (oldItem: T, newItem: T) -> Boolean = { old, new ->
        old == new
    },
) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = areItemsTheSameCallback(oldItem, newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = areContentsTheSameCallback(oldItem, newItem)
}
