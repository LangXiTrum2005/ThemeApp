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

package com.onetabb.template.android.presentation.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.onetabb.template.android.presentation.main.tab.TabPlaceholderFragment

/**
 * TODO(project): thay từng nhánh bằng Fragment thật của app. Fragment mới BẮT BUỘC kế thừa
 * BaseFragment và override `adsScreenId` bằng hằng số trong AdsScreen.
 */
class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = MainPage.COUNT

    override fun createFragment(position: Int): Fragment = when (position) {
        MainPage.TAB2 -> TabPlaceholderFragment.newInstance(MainPage.TAB2)
        MainPage.TAB3 -> TabPlaceholderFragment.newInstance(MainPage.TAB3)
        MainPage.SETTINGS -> TabPlaceholderFragment.newInstance(MainPage.SETTINGS)
        else -> TabPlaceholderFragment.newInstance(MainPage.HOME)
    }
}

object MainPage {
    const val HOME = 0
    const val TAB2 = 1
    const val TAB3 = 2
    const val SETTINGS = 3
    const val COUNT = 4
}

/** Cho Fragment con yêu cầu Activity đổi tab (vd nút "xem tất cả" ở Home). */
interface MainPageHost {
    fun selectPage(page: Int)
}
