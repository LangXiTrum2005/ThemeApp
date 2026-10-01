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

package thinklife.prism.wallpaper.widget.theme.core.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import thinklife.prism.wallpaper.widget.theme.R
import thinklife.prism.wallpaper.widget.theme.databinding.ViewAppButtonBinding

/**
 * Nút dùng chung của app: một dòng chữ, một icon tuỳ chọn đứng trước, và một icon quảng cáo
 * tuỳ chọn đứng sau.
 *
 * Dùng cho MỌI nút bấm thay vì `Button` trần, để ba thứ này chỉ khai một chỗ: nền/màu chữ theo
 * [Style], khoảng cách icon, và quy ước "bấm vào sẽ mở quảng cáo thì phải báo trước bằng icon".
 *
 * ```xml
 * <thinklife.prism.wallpaper.widget.theme.core.widget.AppButton
 *     android:id="@+id/btnWatch"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:abText="@string/watch_to_unlock"
 *     app:abStyle="primary"
 *     app:abShowAdIcon="true" />
 * ```
 *
 * ```kotlin
 * binding.btnWatch.setOnClickListener { … }          // như View thường
 * binding.btnWatch.isAdIconVisible = hasRewardAd     // bật/tắt icon quảng cáo lúc chạy
 * ```
 */
class AppButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    /** Bộ nền + màu chữ. Thêm kiểu mới thì thêm ở đây VÀ trong `attrs.xml`, giữ đúng thứ tự. */
    enum class Style(@DrawableRes val background: Int?, val contentColor: Int) {
        PRIMARY(R.drawable.bg_btn_primary_gradient, R.color.on_accent),
        OUTLINE(R.drawable.bg_btn_outline, R.color.accent),
        TEXT(null, R.color.accent),
    }

    private val binding = ViewAppButtonBinding.inflate(LayoutInflater.from(context), this)

    var text: CharSequence?
        get() = binding.tvText.text
        set(value) {
            binding.tvText.text = value
        }

    /** Icon quảng cáo ở cuối nút. Bật khi hành động sẽ mở reward / inter / native full-screen. */
    var isAdIconVisible: Boolean
        get() = binding.ivAdIcon.isVisible
        set(value) {
            binding.ivAdIcon.isVisible = value
        }

    var style: Style = Style.PRIMARY
        set(value) {
            field = value
            applyStyle(value)
        }

    init {
        orientation = HORIZONTAL
        gravity = android.view.Gravity.CENTER
        minimumHeight = resources.getDimensionPixelSize(R.dimen.app_button_min_height)
        val paddingH = resources.getDimensionPixelSize(R.dimen.app_button_padding_horizontal)
        val paddingV = resources.getDimensionPixelSize(R.dimen.app_button_padding_vertical)
        setPadding(paddingH, paddingV, paddingH, paddingV)
        // Cả nút là một vùng bấm: con không nhận touch, nếu không thì chạm trúng chữ lại trượt click.
        isClickable = true
        isFocusable = true
        descendantFocusability = FOCUS_BLOCK_DESCENDANTS

        context.obtainStyledAttributes(attrs, R.styleable.AppButton).use { array ->
            text = array.getString(R.styleable.AppButton_abText)
            setIcon(array.getResourceId(R.styleable.AppButton_abIcon, 0).takeIf { it != 0 })
            isAdIconVisible = array.getBoolean(R.styleable.AppButton_abShowAdIcon, false)
            style = Style.entries.getOrElse(
                array.getInt(R.styleable.AppButton_abStyle, Style.PRIMARY.ordinal),
            ) { Style.PRIMARY }
        }
    }

    fun setText(@StringRes resId: Int) {
        binding.tvText.setText(resId)
    }

    /** Icon riêng của hành động, đứng trước chữ. Truyền `null` để bỏ icon (và bỏ luôn chỗ trống). */
    fun setIcon(@DrawableRes resId: Int?) {
        binding.ivIcon.isVisible = resId != null
        if (resId != null) binding.ivIcon.setImageResource(resId)
    }

    /** Đồng bộ màu chữ và màu CẢ HAI icon theo style — icon lấy màu bằng tint, không hardcode. */
    private fun applyStyle(style: Style) {
        val color = ContextCompat.getColorStateList(context, style.contentColor)
        binding.tvText.setTextColor(color)
        ImageViewCompat.setImageTintList(binding.ivIcon, color)
        ImageViewCompat.setImageTintList(binding.ivAdIcon, color)
        setBackgroundResource(style.background ?: 0)
        if (style.background == null) {
            // Nút chỉ có chữ vẫn cần phản hồi khi chạm.
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackgroundBorderless,
                outValue,
                true,
            )
            setBackgroundResource(outValue.resourceId)
        }
    }
}
