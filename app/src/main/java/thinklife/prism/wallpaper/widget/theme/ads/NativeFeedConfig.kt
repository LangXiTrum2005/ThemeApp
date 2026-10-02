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

import org.json.JSONObject
import org.json.JSONArray

/** Remote Config cũ chỉ có một slot feed; bổ sung ID container, giữ nguyên rule của AdOps. */
internal fun completeFeedSlotConfig(raw: String): String = runCatching {
    val config = JSONObject(raw)
    val slots = config.optJSONObject("slots") ?: return@runCatching raw
    val inventories = config.optJSONObject("inventories")
    val baseInventory = inventories?.optJSONObject(AdsInventory.NATIVE_HOME)
    if (inventories != null && baseInventory != null &&
        AdsInventory.nativeFeedPool.drop(1).any { !inventories.has(it) }
    ) {
        // Firebase cũ chưa tách inventory: cache chung sẽ bind cùng một ad vào nhiều hàng.
        val feedScreens = config.optJSONObject("screens")
        val preloadTriggers = JSONArray()
        if (feedScreens != null) {
            val bases = AdsSlot.feedSlotGroups.map { it.first() }.toSet()
            for (screen in feedScreens.keys()) {
                val ids = feedScreens.getJSONObject(screen).optJSONArray("slots") ?: continue
                if ((0 until ids.length()).any { ids.getString(it) in bases }) {
                    preloadTriggers.put(JSONObject().put("type", "screen_enter").put("screen", screen))
                }
            }
        }
        val strategy = baseInventory.optJSONObject("load_strategy") ?: JSONObject()
        strategy.put("mode", "preload").put("sticky", true).put("reload_after_render", true)
            .put("cache_ttl_sec", FEED_CACHE_TTL_SEC).put("preload_triggers", preloadTriggers)
        baseInventory.put("load_strategy", strategy)
        for (id in AdsInventory.nativeFeedPool.drop(1)) {
            if (!inventories.has(id)) {
                inventories.put(id, JSONObject(baseInventory.toString()).put("inventory_id", id))
            }
        }
    }
    for (group in AdsSlot.feedSlotGroups) {
        val base = group.first()
        val baseConfig = slots.optJSONObject(base) ?: continue
        for ((index, slot) in group.withIndex().drop(1)) {
            if (!slots.has(slot)) slots.put(slot, JSONObject(baseConfig.toString()))
            val alias = slots.getJSONObject(slot)
            if (baseConfig.optString("inventory_id") == AdsInventory.NATIVE_HOME &&
                alias.optString("inventory_id") == AdsInventory.NATIVE_HOME
            ) {
                alias.put("inventory_id", AdsInventory.nativeFeedPool[index])
            }
        }
        val screens = config.optJSONObject("screens") ?: continue
        for (screen in screens.keys()) {
            val screenSlots = screens.getJSONObject(screen).optJSONArray("slots") ?: continue
            val existing = (0 until screenSlots.length()).map { screenSlots.getString(it) }.toSet()
            if (base !in existing) continue
            for (slot in group.drop(1)) {
                if (slot !in existing) screenSlots.put(slot)
            }
        }
    }
    config.toString()
}.getOrDefault(raw)

private const val FEED_CACHE_TTL_SEC = 1800
