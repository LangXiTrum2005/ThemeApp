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

package com.ontabb.rate

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * API công khai của module đánh giá.
 *
 * Kiểu hiển thị (hộp thoại giữa màn hay bottom sheet) chọn bằng [RateConfig.style].
 *
 * Vòng đời:
 *  1. `Application.onCreate` -> [init] + [recordAppOpen].
 *  2. Màn Home -> [showOnHome] mỗi lần user vào / quay lại.
 *  3. Back của MÁY ở màn cuối (định thoát app) -> [showOnBackPressed].
 *  4. Nút "Rate us" trong Settings -> [showOnDemand].
 *
 * Ba điểm kích hoạt tự động:
 *
 *  | Điểm kích hoạt      | Xét ở                | Điều kiện                                    |
 *  |---------------------|----------------------|----------------------------------------------|
 *  | Lần mở app thứ N    | [showOnHome]         | [RateConfig.secondAppOpenAt], MỘT lần đời máy |
 *  | Quay lại Home       | [showOnHome]         | lần thứ nằm trong [RateConfig.showOnHomeAt]   |
 *  | Bấm back thoát app  | [showOnBackPressed]  | lần thứ nằm trong [RateConfig.showOnBackAt]   |
 *
 * Hai danh sách sau do Remote Config điều khiển, app bơm xuống bằng [updateShowRules]. Cả ba
 * điểm TỰ ĐỘNG còn chịu chung một công tắc [RateConfig.isEnabled] (xem [setEnabled]); riêng
 * [showOnDemand] nằm ngoài công tắc đó.
 *
 * Luật xử lý số sao:
 *  - 1-3 sao -> mở [FeedbackActivity] để user viết góp ý, gửi về [RateConfig.contactEmail].
 *  - 4-5 sao -> mở thẳng trang app trên Play Store để user tự đánh giá ở đó.
 *
 * Cả hai nhánh đều đánh dấu "đã đánh giá" nên mọi điểm kích hoạt tự động im vĩnh viễn sau đó.
 */
object OneTabbRate {

    private const val TAG = "OneTabbRate"

    private const val PREFS_NAME = "ontabb_rate_prefs"
    private const val KEY_APP_OPEN_COUNT = "app_open_count"
    private const val KEY_SHOWN_ON_NTH_OPEN = "shown_on_nth_open"
    private const val KEY_HOME_COUNT = "home_count"
    private const val KEY_BACK_COUNT = "back_count"
    private const val KEY_HAS_RATED = "has_rated"

    private val LOW_RATING = 1..3
    private val HIGH_RATING = 4..5

    @Volatile
    private var config: RateConfig? = null

    /** Chặn bung chồng hộp thoại khi Activity resume lại lúc hộp thoại đang mở. */
    @Volatile
    private var isDialogShowing = false

    internal fun requireConfig(): RateConfig = config
        ?: error("Chưa gọi OneTabbRate.init(RateConfig) ở Application.onCreate")

    /** Nạp cấu hình. Gọi ở `Application.onCreate`, trước mọi điểm kích hoạt. */
    fun init(config: RateConfig) {
        this.config = config
    }

    /**
     * Bật/tắt toàn bộ điểm kích hoạt TỰ ĐỘNG. Nguồn thường là khoá `enable` của Remote Config.
     *
     * Tắt xong thì [showOnHome] và [showOnBackPressed] trả false ngay và KHÔNG tăng bộ đếm —
     * bật lại user vẫn ở đúng lượt cũ, không bị nhảy qua mấy lượt lẽ ra được bung. Nút "Rate us"
     * ([showOnDemand]) không đi qua công tắc này.
     */
    fun setEnabled(isEnabled: Boolean) {
        val current = config ?: return
        if (current.isEnabled == isEnabled) return
        config = current.copy(isEnabled = isEnabled)
        Log.d(TAG, "setEnabled: $isEnabled")
    }

    /**
     * Ghi đè hai danh sách lần-bung bằng giá trị Remote Config. Gọi được nhiều lần, bất cứ lúc
     * nào sau [init]; truyền `null` cho danh sách muốn giữ nguyên.
     *
     * Bộ đếm KHÔNG bị đụng tới: đổi danh sách giữa chừng chỉ đổi việc lần tới có bung hay không,
     * không làm user đang ở lần thứ 5 tụt về 0.
     */
    fun updateShowRules(showOnHomeAt: Set<Int>? = null, showOnBackAt: Set<Int>? = null) {
        val current = config ?: return
        config = current.copy(
            showOnHomeAt = showOnHomeAt ?: current.showOnHomeAt,
            showOnBackAt = showOnBackAt ?: current.showOnBackAt,
        )
        Log.d(TAG, "updateShowRules: home=${config?.showOnHomeAt}, back=${config?.showOnBackAt}")
    }

    // ─── Bộ đếm ──────────────────────────────────────────────────────────────────────────

    /**
     * Tăng bộ đếm số lần mở app. Gọi ở `Application.onCreate` - tức mỗi lần tiến trình được
     * dựng lại, KHÔNG phải mỗi lần Activity resume.
     */
    fun recordAppOpen(context: Context) {
        val prefs = context.prefs()
        val current = prefs.getInt(KEY_APP_OPEN_COUNT, 0)
        if (current == Int.MAX_VALUE) return
        prefs.edit { putInt(KEY_APP_OPEN_COUNT, current + 1) }
        Log.d(TAG, "recordAppOpen: lần mở thứ ${current + 1}")
    }

    /** User đã đánh giá chưa (tính cả trường hợp gửi góp ý khi chấm thấp). */
    fun hasRated(context: Context): Boolean = context.prefs().getBoolean(KEY_HAS_RATED, false)

    internal fun markAsRated(context: Context) {
        context.prefs().edit { putBoolean(KEY_HAS_RATED, true) }
    }

    // ─── Điểm kích hoạt ──────────────────────────────────────────────────────────────────

    /**
     * Gọi mỗi khi user vào / quay lại màn Home. Trả về true nếu hộp thoại được bung.
     *
     * Bộ đếm Home tăng ngay cả ở lượt được kích hoạt bởi "lần mở app thứ N", để dãy 2-4-6 không
     * bị xô lệch tuỳ theo việc lượt kia có bắn hay không.
     */
    fun showOnHome(activity: FragmentActivity): Boolean {
        val cfg = requireConfig()
        if (!cfg.isEnabled) {
            Log.d(TAG, "home: bỏ qua, rating đang tắt")
            return false
        }
        if (hasRated(activity)) {
            Log.d(TAG, "bỏ qua: user đã đánh giá rồi")
            return false
        }
        if (isDialogShowing) {
            Log.d(TAG, "bỏ qua: hộp thoại đang hiển thị")
            return false
        }

        val prefs = activity.prefs()
        val homeCount = prefs.getInt(KEY_HOME_COUNT, 0) + 1
        prefs.edit { putInt(KEY_HOME_COUNT, homeCount) }

        val appOpenCount = prefs.getInt(KEY_APP_OPEN_COUNT, 0)
        val isNthOpenDue = cfg.secondAppOpenAt > 0 &&
            appOpenCount == cfg.secondAppOpenAt &&
            !prefs.getBoolean(KEY_SHOWN_ON_NTH_OPEN, false)
        val isHomeDue = homeCount in cfg.showOnHomeAt

        if (!isNthOpenDue && !isHomeDue) {
            Log.d(TAG, "không bung: mở app lần $appOpenCount, về Home lần $homeCount")
            return false
        }

        val isShown = show(activity, ignoreAlreadyRated = false)
        if (isShown && isNthOpenDue) {
            prefs.edit { putBoolean(KEY_SHOWN_ON_NTH_OPEN, true) }
        }
        Log.d(
            TAG,
            "bung=$isShown (mở app lần $appOpenCount, về Home lần $homeCount, " +
                "lần-mở-thứ-N=$isNthOpenDue, home=$isHomeDue)",
        )
        return isShown
    }

    /**
     * Gọi khi user bấm nút back của MÁY ở màn cuối, tức là đang định thoát app.
     *
     * Trả về true nghĩa là hộp thoại đã bung và nơi gọi PHẢI hoãn việc thoát — không thì user
     * thấy hộp thoại nhấp nháy rồi app đóng luôn. Trả false thì cứ thoát như bình thường.
     */
    fun showOnBackPressed(activity: FragmentActivity): Boolean {
        val cfg = requireConfig()
        if (!cfg.isEnabled) {
            Log.d(TAG, "back: bỏ qua, rating đang tắt")
            return false
        }
        if (cfg.showOnBackAt.isEmpty()) return false
        if (hasRated(activity)) {
            Log.d(TAG, "back: bỏ qua, user đã đánh giá rồi")
            return false
        }
        if (isDialogShowing) return false

        val prefs = activity.prefs()
        val backCount = prefs.getInt(KEY_BACK_COUNT, 0) + 1
        prefs.edit { putInt(KEY_BACK_COUNT, backCount) }

        if (backCount !in cfg.showOnBackAt) {
            Log.d(TAG, "back: không bung, lần thoát thứ $backCount")
            return false
        }
        val isShown = show(activity, ignoreAlreadyRated = false)
        Log.d(TAG, "back: bung=$isShown (lần thoát thứ $backCount)")
        return isShown
    }

    /**
     * Bung hộp thoại theo yêu cầu trực tiếp của user (nút "Rate us"). Bỏ qua mọi bộ đếm và cả
     * cờ "đã đánh giá rồi": cờ đó sinh ra để chặn app làm phiền, không phải để từ chối user tự
     * tay bấm. Cũng KHÔNG đi qua công tắc [RateConfig.isEnabled]: công tắc đó để chặn app tự
     * làm phiền, không phải chặn user.
     *
     * Trả về false khi không bung được - lúc đó nơi gọi nên tự [openStoreListing].
     */
    fun showOnDemand(activity: FragmentActivity): Boolean {
        requireConfig()
        if (isDialogShowing) return true
        return show(activity, ignoreAlreadyRated = true)
    }

    /** Mở trang app trên Play Store. Dùng làm phương án cuối khi [showOnDemand] trả về false. */
    fun openStoreListing(context: Context) {
        context.openPlayStoreListing(storeIdOf(context))
    }

    // ─── Bung hộp thoại + xử lý số sao ───────────────────────────────────────────────────

    private fun show(activity: FragmentActivity, ignoreAlreadyRated: Boolean): Boolean {
        if (!ignoreAlreadyRated && hasRated(activity)) return false
        if (activity.isFinishing || activity.isDestroyed) return false

        val fm = activity.supportFragmentManager
        // Commit sau onSaveInstanceState sẽ ném IllegalStateException; thà bỏ một lượt hiển thị
        // còn hơn crash.
        if (fm.isStateSaved) return false

        val style = requireConfig().style
        val tag = TAG_BY_STYLE.getValue(style)
        // Kiểm cả hai tag: app đổi style qua Remote Config giữa hai lượt thì lượt cũ có thể còn
        // nằm đó dưới tag kia, bung thêm cái nữa là chồng hai lớp mờ lên nhau.
        if (TAG_BY_STYLE.values.any { fm.findFragmentByTag(it) != null }) return false

        fm.setFragmentResultListener(RateResult.REQUEST_KEY, activity) { _, result ->
            handleRating(activity, result.getInt(RateResult.KEY_RATING))
        }

        when (style) {
            RateStyle.DIALOG -> RateAppDialog.newInstance().show(fm, tag)
            RateStyle.BOTTOM_SHEET -> RateAppBottomSheet.newInstance().show(fm, tag)
        }
        isDialogShowing = true
        // Khối đánh giá đóng được từ ba đường (nút X, nút Rate, back) — riêng bottom sheet còn
        // vuốt xuống được nữa — nên bắt ở vòng đời của chính nó thay vì nối vào từng đường một.
        fm.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentDestroyed(fragmentManager: FragmentManager, fragment: Fragment) {
                    if (fragment is RateAppDialog || fragment is RateAppBottomSheet) {
                        isDialogShowing = false
                        fragmentManager.unregisterFragmentLifecycleCallbacks(this)
                    }
                }
            },
            false,
        )
        return true
    }

    private fun handleRating(activity: FragmentActivity, rating: Int) {
        when (rating) {
            in LOW_RATING -> {
                markAsRated(activity)
                activity.startActivity(FeedbackActivity.newIntent(activity, rating))
            }

            in HIGH_RATING -> {
                markAsRated(activity)
                openStoreListing(activity)
            }

            else -> Log.w(TAG, "số sao không hợp lệ: $rating")
        }
    }

    private val TAG_BY_STYLE = mapOf(
        RateStyle.DIALOG to RateAppDialog::class.java.simpleName,
        RateStyle.BOTTOM_SHEET to RateAppBottomSheet::class.java.simpleName,
    )

    private fun storeIdOf(context: Context): String = config?.storeId ?: context.packageName

    private fun Context.prefs() = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
