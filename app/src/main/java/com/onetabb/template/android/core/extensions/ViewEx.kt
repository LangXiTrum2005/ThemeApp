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

@file:Suppress("UNCHECKED_CAST")

package com.onetabb.template.android.core.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.Rect
import android.net.Uri
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import java.lang.reflect.Field
import java.util.Timer
import kotlin.concurrent.schedule

fun View.onAvoidDoubleClick(
    throttleDelay: Long = 600L,
    onClick: (View) -> Unit,
) {
    var lastClickTime = 0L
    setOnClickListener { view ->
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime < throttleDelay) return@setOnClickListener
        lastClickTime = now
        onClick(view)
    }
}

fun View.setSingleClickListener(
    intervalMs: Long = 600L,
    onClick: (View) -> Unit,
) = onAvoidDoubleClick(intervalMs, onClick)

fun View.setAnimation(context: Activity, anim: Int?, duration: Long, onAnimation: (View) -> Unit) {
    this.startAnimation(anim?.let { AnimationUtils.loadAnimation(context, it) })
    Timer().schedule(duration) {
        context.runOnUiThread {
            onAnimation(this@setAnimation)
        }
    }
}

fun View.visible() {
    if (visibility != View.VISIBLE) {
        this.visibility = View.VISIBLE
    }
}

fun View.invisible() {
    if (visibility != View.INVISIBLE) {
        this.visibility = View.INVISIBLE
    }
}

fun View.gone() {
    if (visibility != View.GONE) {
        this.visibility = View.GONE
    }
}

fun View.visibleBy(visible: Boolean) {
    if (visible) visible() else gone()
}

fun View.invisibleBy(visible: Boolean) {
    if (visible) invisible() else visible()
}

fun View.doOnViewDrawn(removeCallbackBy: (() -> Boolean) = { true }, onDrawn: () -> Unit) {
    var global: ViewTreeObserver.OnGlobalLayoutListener? = null
    global = ViewTreeObserver.OnGlobalLayoutListener {
        if (removeCallbackBy()) {
            onDrawn()
            viewTreeObserver.removeOnGlobalLayoutListener(global)
        }
    }
    viewTreeObserver.addOnGlobalLayoutListener(global)
}

fun View.doOnViewDrawnRepeat(removeCallbackBy: (() -> Boolean) = { true }, onDrawn: () -> Unit) {
    var global: ViewTreeObserver.OnGlobalLayoutListener? = null
    global = ViewTreeObserver.OnGlobalLayoutListener {
        onDrawn()
        if (removeCallbackBy()) {
            viewTreeObserver.removeOnGlobalLayoutListener(global)
        }
    }
    viewTreeObserver.addOnGlobalLayoutListener(global)
}

fun ImageView.tintImage(color: Int) {
    setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
}

fun View.toBitMap(): Bitmap? {
    if (measuredWidth * measuredHeight == 0) return null
    val b = createBitmap(measuredWidth, measuredHeight)
    val c = Canvas(b)
    layout(left, top, right, bottom)
    draw(c)
    return b
}

fun View.listenKeyBoardToggle(onKeyBoardToggle: (Boolean) -> Unit) {
    doOnViewDrawnRepeat({ false }) {
        val r = Rect()
        getWindowVisibleDisplayFrame(r)
        val screenHeight: Int = rootView.height
        val keypadHeight: Int = screenHeight - r.bottom
        onKeyBoardToggle(keypadHeight > screenHeight * 0.15)
    }
}

inline fun <reified C> Context.getResID(resName: String): Int = try {
    val idField: Field = C::class.java.getDeclaredField(resName)
    idField.getInt(idField)
} catch (e: java.lang.Exception) {
    e.printStackTrace()
    -1
}

fun View.hostedActivity() = run {
    var check = context
    while (check is ContextWrapper) {
        if (check is Activity) {
            return@run check
        }
        check = check.baseContext
    }
    return@run null
}

fun View.showKeyboard() {
    requestFocus()

    post {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE)
            as InputMethodManager

        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.hideKeyboard() {
    clearFocus()

    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE)
        as InputMethodManager

    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Biến một phần của TextView thành link mở URL khi click.
 *
 * @param linkText phần chuỗi hiển thị như link (phải khớp đúng với nội dung text hiện tại)
 * @param url URL sẽ mở khi người dùng click (http(s)://...)
 * @param linkColor optional màu link (resource color id). Nếu null dùng màu textColorLink mặc định.
 */
fun TextView.makeLinkOpenUrl(linkText: String, url: String, linkColorRes: Int? = null) {
    val full = text?.toString() ?: return
    val start = full.indexOf(linkText)
    if (start == -1) return

    val spannable = SpannableString(full)
    val end = start + linkText.length

    val clickable = object : ClickableSpan() {
        override fun onClick(widget: View) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // optional: fallback hoặc thông báo lỗi
            }
        }
    }

    // đặt ClickableSpan
    spannable.setSpan(clickable, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    // underline (nếu muốn)
    spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    this.text = spannable
    this.movementMethod = LinkMovementMethod.getInstance()

    linkColorRes?.let {
        try {
            val color = ContextCompat.getColor(context, it)
            this.setLinkTextColor(color)
        } catch (_: Exception) {
        }
    }
}

/**
 * Biến một phần của TextView thành link gọi callback khi click.
 *
 * @param linkText phần chuỗi hiển thị như link (phải khớp đúng với nội dung text hiện tại)
 * @param onClick callback khi click
 * @param underline true nếu muốn gạch chân
 */
fun TextView.makeLinkAction(linkText: String, underline: Boolean = true, onClick: (View) -> Unit) {
    val full = text?.toString() ?: return
    val start = full.indexOf(linkText)
    if (start == -1) return

    val spannable = SpannableString(full)
    val end = start + linkText.length

    val clickable = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick(widget)
        }
    }

    spannable.setSpan(clickable, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    if (underline) {
        spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    this.text = spannable
    this.movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.colorPartText(
    fullText: String,
    target: String,
    color: Int,
) {
    try {
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf(target)
        if (start < 0) {
            text = fullText
            return
        }

        spannable.setSpan(
            ForegroundColorSpan(color),
            start,
            start + target.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        this.text = spannable
    } catch (_: Exception) {}
}
