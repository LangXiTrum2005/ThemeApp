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

package thinklife.prism.wallpaper.widget.theme.presentation.dialog

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import thinklife.prism.wallpaper.widget.theme.R
import kotlinx.parcelize.Parcelize

/**
 * Nội dung của [PermissionDialog] cho MỘT quyền. Mỗi quyền một bộ icon + chữ, còn khung dialog
 * dùng chung.
 *
 * Truyền resource id chứ không truyền chuỗi đã dựng sẵn: dialog sống qua xoay màn và đổi ngôn
 * ngữ, chuỗi dựng sẵn sẽ giữ nguyên tiếng cũ sau khi user đổi ngôn ngữ.
 *
 * @param negativeText đặt `null` để ẩn hẳn nút phụ (quyền bắt buộc, không có đường "để sau").
 */
@Parcelize
data class PermissionDialogArgs(
    @param:DrawableRes val icon: Int,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    @param:StringRes val positiveText: Int = R.string.permission_allow,
    @param:StringRes val negativeText: Int? = R.string.permission_not_now,
) : Parcelable
