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

package thinklife.prism.wallpaper.widget.theme.ads

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.junit.Assert.assertEquals
import org.junit.Test

class NativeFeedAdapterTest {
    @Test
    fun adsFollowEverySixContentsWithoutChangingContentIndices() {
        val content = TestContentAdapter(18)
        val adapter = NativeFeedAdapter(
            content, AdsScreen.THEME_CATEGORY_VIEW, AdsSlot.THEME_CATEGORY_SLOT_FEED, TestOwner(),
        )
        assertEquals(
            listOf(0, 1, 2, 3, 4, 5, AD_TYPE, 6, 7, 8, 9, 10, 11, AD_TYPE, 12, 13, 14, 15, 16, 17, AD_TYPE),
            (0 until adapter.itemCount).map(adapter::getItemViewType),
        )
        assertEquals(18, content.itemCount)
    }

    @Test
    fun shorterListsHaveNoAdBeforeSixContents() {
        for (count in 0..5) {
            val adapter = NativeFeedAdapter(
                TestContentAdapter(count), AdsScreen.THEME_CATEGORY_VIEW,
                AdsSlot.THEME_CATEGORY_SLOT_FEED, TestOwner(),
            )
            assertEquals(count, adapter.itemCount)
        }
    }

    private class TestContentAdapter(private val count: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount() = count
        override fun getItemViewType(position: Int) = position
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            error("View inflation is not needed for position mapping")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit
    }

    private class TestOwner : LifecycleOwner {
        override val lifecycle = object : Lifecycle() {
            override val currentState = State.CREATED
            override fun addObserver(observer: LifecycleObserver) = Unit
            override fun removeObserver(observer: LifecycleObserver) = Unit
        }
    }

    private companion object {
        const val AD_TYPE = Int.MIN_VALUE
    }
}
