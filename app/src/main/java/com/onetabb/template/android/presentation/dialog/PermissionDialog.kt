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

package com.onetabb.template.android.presentation.dialog

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LifecycleOwner
import com.onetabb.template.android.ads.AdsScreen
import com.onetabb.template.android.ads.AdsTrigger
import com.onetabb.template.android.core.base.BaseDialogFragment
import com.onetabb.template.android.core.extensions.getParcel
import com.onetabb.template.android.databinding.DialogPermissionBinding

/**
 * Dialog giải thích vì sao app cần một quyền, trước khi bật hộp thoại quyền của hệ thống.
 *
 * Khung dialog dùng chung cho MỌI quyền; icon và chữ truyền qua [PermissionDialogArgs]:
 *
 * ```kotlin
 * PermissionDialog.show(
 *     supportFragmentManager,
 *     PermissionDialogArgs(
 *         icon = R.drawable.ic_permission_notification,
 *         title = R.string.permission_notification_title,
 *         description = R.string.permission_notification_description,
 *     ),
 * )
 * PermissionDialog.setResultListener(supportFragmentManager, this) { isAccepted ->
 *     if (isAccepted) requestNotiPermissionLauncher.launch() else goNext()
 * }
 * ```
 *
 * Dialog KHÔNG tự xin quyền: nó chỉ trả lời "user có đồng ý đi tiếp không" qua
 * [REQUEST_KEY]. Việc gọi launcher nằm ở màn gọi, vì mỗi màn xin một quyền khác nhau.
 *
 * Khi hệ thống KHÔNG còn hiện hộp thoại quyền (user đã chặn vĩnh viễn), gọi launcher là vô ích:
 * hỏi `PermissionHelper.mustOpenSettings(...)` trước, đúng thì đổi bộ chữ sang bản "mở Settings"
 * và ở nhánh đồng ý gọi `PermissionHelper.openSettings(...)`. Xem ví dụ đầy đủ ở `PermissionHelper`.
 */
class PermissionDialog : BaseDialogFragment<DialogPermissionBinding>() {

    override fun bindingView(): DialogPermissionBinding = DialogPermissionBinding.inflate(layoutInflater)

    override val adsScreenId = AdsScreen.PERMISSION_DIALOG_VIEW

    private val args by lazy {
        checkNotNull(arguments?.getParcel<PermissionDialogArgs>(ARG_CONTENT)) {
            "Thiếu PermissionDialogArgs — dựng dialog bằng PermissionDialog.newInstance()"
        }
    }

    override fun initConfig(view: View, savedInstanceState: Bundle?) {
        binding.ivIcon.setImageResource(args.icon)
        binding.tvTitle.setText(args.title)
        binding.tvDescription.setText(args.description)
        binding.btnPositive.setText(args.positiveText)

        // negativeText = null nghĩa là quyền bắt buộc: bỏ luôn nút phụ thay vì để nút chết.
        val negativeText = args.negativeText
        binding.btnNegative.isVisible = negativeText != null
        if (negativeText != null) binding.btnNegative.setText(negativeText)
    }

    override fun initListener() {
        binding.btnPositive.setOnClickListener {
            triggerAds(AdsTrigger.PERMISSION_CLICK_ALLOW) {
                runWhenResumed { finishWith(isAccepted = true) }
            }
        }
        binding.btnNegative.setOnClickListener {
            triggerAds(AdsTrigger.PERMISSION_CLICK_DENY) {
                runWhenResumed { finishWith(isAccepted = false) }
            }
        }
    }

    /** Back cũng là một câu trả lời "không" — bắn kết quả rồi mới đóng, tránh nơi gọi treo. */
    override fun onCancel(dialog: android.content.DialogInterface) {
        setResult(isAccepted = false)
        super.onCancel(dialog)
    }

    private fun finishWith(isAccepted: Boolean) {
        setResult(isAccepted)
        dismissAllowingStateLoss()
    }

    private fun setResult(isAccepted: Boolean) {
        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(KEY_ACCEPTED to isAccepted))
    }

    companion object {
        const val REQUEST_KEY = "permission_dialog_result"
        const val KEY_ACCEPTED = "permission_dialog_accepted"

        private const val ARG_CONTENT = "arg_content"
        private const val TAG = "PermissionDialog"

        fun newInstance(args: PermissionDialogArgs) = PermissionDialog().apply {
            arguments = bundleOf(ARG_CONTENT to args)
        }

        /**
         * Bung dialog. Bỏ qua nếu đang có một cái mở sẵn hoặc FragmentManager đã lưu state —
         * commit lúc đó ném IllegalStateException, thà bỏ một lượt còn hơn crash.
         */
        fun show(fragmentManager: FragmentManager, args: PermissionDialogArgs) {
            if (fragmentManager.isStateSaved) return
            if (fragmentManager.findFragmentByTag(TAG) != null) return
            newInstance(args).show(fragmentManager, TAG)
        }

        /** Nghe kết quả. Đăng ký sớm (onCreate) để không lỡ kết quả sau khi Activity dựng lại. */
        fun setResultListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onResult: (isAccepted: Boolean) -> Unit,
        ) {
            fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, bundle ->
                onResult(bundle.getBoolean(KEY_ACCEPTED, false))
            }
        }
    }
}

/** Bản tiện dụng cho Fragment: dùng luôn `childFragmentManager` và `viewLifecycleOwner`. */
fun androidx.fragment.app.Fragment.setPermissionDialogResultListener(
    onResult: (isAccepted: Boolean) -> Unit,
) = setFragmentResultListener(PermissionDialog.REQUEST_KEY) { _, bundle ->
    onResult(bundle.getBoolean(PermissionDialog.KEY_ACCEPTED, false))
}
