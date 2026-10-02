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

import com.adshub.monetization.model.AdsScenarioConfig
import java.io.File
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdsConfigTest {
    private val raw = File("src/main/assets/ads_config.json").readText()
    private val config = JSONObject(raw)
    private val sdkJson = Json { ignoreUnknownKeys = true }

    @Test
    fun configIsAcceptedByInstalledSdk() {
        val decoded = sdkJson.decodeFromString(AdsScenarioConfig.serializer(), raw)
        assertEquals(config.getInt("config_version"), decoded.configVersion)
    }

    @Test
    fun feedPoolsHaveThreeDistinctInventoriesWithPreloadAndStickyCache() {
        val slots = config.getJSONObject("slots")
        val inventories = config.getJSONObject("inventories")
        val seen = mutableSetOf<String>()
        for (group in AdsSlot.feedSlotGroups) {
            assertEquals(3, group.size)
            assertEquals(AdsInventory.nativeFeedPool, group.map { slots.getJSONObject(it).getString("inventory_id") })
            for (id in group) {
                assertTrue("Duplicate feed slot: $id", seen.add(id))
            }
        }
        for (id in AdsInventory.nativeFeedPool) {
            val inventory = inventories.getJSONObject(id)
            assertEquals(id, inventory.getString("inventory_id"))
            assertEquals("native_home", inventory.getString("waterfall_id"))
            assertEquals("native_small", inventory.getString("native_style_id"))
            val strategy = inventory.getJSONObject("load_strategy")
            assertEquals("preload", strategy.getString("mode"))
            assertTrue(strategy.getBoolean("sticky"))
            assertTrue(strategy.getBoolean("reload_after_render"))
            val preloads = strategy.getJSONArray("preload_triggers")
            assertEquals(AdsSlot.feedSlotGroups.size, preloads.length())
            for (index in 0 until preloads.length()) {
                assertTrue(config.getJSONObject("screens").has(preloads.getJSONObject(index).getString("screen")))
            }
        }
    }

    @Test
    fun legacyRemoteConfigExpandsFeedSlotsWithoutEnablingDisabledAds() {
        val base = AdsSlot.THEME_CATEGORY_SLOT_FEED
        val group = AdsSlot.feedSlots(base)
        val legacy = JSONObject(raw)
        val slots = legacy.getJSONObject("slots")
        group.drop(1).forEach(slots::remove)
        val inventories = legacy.getJSONObject("inventories")
        AdsInventory.nativeFeedPool.drop(1).forEach(inventories::remove)
        val oldInventory = inventories.getJSONObject(AdsInventory.NATIVE_HOME)
        oldInventory.put("enabled", false)
        oldInventory.put("load_strategy", JSONObject().put("mode", "render_immediately").put("sticky", false))
        val baseConfig = slots.getJSONObject(base)
        baseConfig.put("enabled", false)
        baseConfig.put("refresh_policy", "on_click")
        val screen = legacy.getJSONObject("screens").getJSONObject(AdsScreen.THEME_CATEGORY_VIEW)
        screen.put("slots", org.json.JSONArray(listOf(base)))

        val expanded = JSONObject(completeFeedSlotConfig(legacy.toString()))
        val expandedSlots = expanded.getJSONObject("slots")
        val screenSlots = expanded.getJSONObject("screens")
            .getJSONObject(AdsScreen.THEME_CATEGORY_VIEW).getJSONArray("slots")
        assertEquals(group.toSet(), (0 until screenSlots.length()).map(screenSlots::getString).toSet())
        for ((index, id) in group.withIndex()) {
            val slot = expandedSlots.getJSONObject(id)
            assertFalse(slot.getBoolean("enabled"))
            assertEquals("on_click", slot.getString("refresh_policy"))
            assertEquals(AdsInventory.nativeFeedPool[index], slot.getString("inventory_id"))
        }
        for (id in AdsInventory.nativeFeedPool) {
            val inventory = expanded.getJSONObject("inventories").getJSONObject(id)
            assertFalse(inventory.getBoolean("enabled"))
            assertEquals("native_home", inventory.getString("waterfall_id"))
            assertEquals("preload", inventory.getJSONObject("load_strategy").getString("mode"))
            assertTrue(inventory.getJSONObject("load_strategy").getBoolean("sticky"))
        }
        sdkJson.decodeFromString(AdsScenarioConfig.serializer(), expanded.toString())
    }

    @Test
    fun remoteFeedExpansionPreservesExplicitOverridesAndDoesNotDuplicateSlots() {
        val remote = JSONObject(raw)
        val id = AdsSlot.feedSlots(AdsSlot.ICONS_SLOT_FEED)[1]
        remote.getJSONObject("slots").getJSONObject(id).put("enabled", false)
        val expanded = completeFeedSlotConfig(remote.toString())
        val secondPass = JSONObject(completeFeedSlotConfig(expanded))
        assertFalse(secondPass.getJSONObject("slots").getJSONObject(id).getBoolean("enabled"))
        val screenSlots = secondPass.getJSONObject("screens").getJSONObject(AdsScreen.ICONS_VIEW)
            .getJSONArray("slots")
        val ids = (0 until screenSlots.length()).map(screenSlots::getString)
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun malformedRemoteConfigIsLeftToSdkValidation() {
        val malformed = "{invalid json"
        assertEquals(malformed, completeFeedSlotConfig(malformed))
    }

    @Test
    fun everyScreenSlotAndTriggerHasAValidContract() {
        for ((type, section) in listOf(
            AdsScreen::class.java to "screens",
            AdsSlot::class.java to "slots",
            AdsTrigger::class.java to "triggers",
        )) {
            val values = type.fields.filter { it.type == String::class.java }
                .map { it.get(null) as String }.toSet()
            assertEquals(section, values, config.getJSONObject(section).keys().asSequence().toSet())
        }
        val screens = config.getJSONObject("screens")
        val triggers = config.getJSONObject("triggers")
        val slots = config.getJSONObject("slots")
        val inventories = config.getJSONObject("inventories")
        val waterfalls = config.getJSONObject("waterfalls")
        val referencedSlots = mutableSetOf<String>()
        for (screen in screens.keys()) {
            val entry = screens.getJSONObject(screen)
            val screenTriggers = entry.getJSONArray("triggers")
            for (index in 0 until screenTriggers.length()) {
                assertEquals(screen, triggers.getJSONObject(screenTriggers.getString(index)).getString("screen"))
            }
            val screenSlots = entry.getJSONArray("slots")
            for (index in 0 until screenSlots.length()) {
                val slot = screenSlots.getString(index)
                assertTrue(slot, slots.has(slot))
                referencedSlots.add(slot)
            }
        }
        assertEquals(slots.keys().asSequence().toSet(), referencedSlots)
        for (section in listOf(slots, triggers)) {
            for (key in section.keys()) {
                val inventory = section.getJSONObject(key).optString("inventory_id")
                assertTrue(key, inventory.isEmpty() || inventories.has(inventory))
            }
        }
        for (key in inventories.keys()) {
            val inventory = inventories.getJSONObject(key)
            assertTrue(key, waterfalls.has(inventory.getString("waterfall_id")))
        }
    }

    @Test
    fun rewardLoadsOnDemandAndRequiresEarnedReward() {
        val inventory = config.getJSONObject("inventories").getJSONObject("inv_reward_apply")
        val strategy = inventory.getJSONObject("load_strategy")
        assertEquals("load_on_demand", strategy.getString("mode"))
        assertEquals(0, strategy.getJSONArray("preload_triggers").length())
        assertFalse(strategy.getBoolean("reload_after_show"))
        val waterfall = config.getJSONObject("waterfalls").getJSONObject(inventory.getString("waterfall_id"))
        assertTrue(inventory.getLong("timeout_ms") >= waterfall.getLong("total_timeout_ms"))
        assertEquals("reward_earned", inventory.getJSONArray("rewards").getJSONObject(0).getString("grant_on"))
        val triggers = config.getJSONObject("triggers")
        for (id in listOf(
            AdsTrigger.PREVIEW_WALLPAPER_CLICK_DOWNLOAD,
            AdsTrigger.GET_THEME_CLICK_START_WALLPAPER_DOWNLOAD,
            AdsTrigger.GET_THEME_CLICK_INSTALL_SHORTCUT,
            AdsTrigger.GET_THEME_CLICK_INSTALL_ALL_SHORTCUTS,
            AdsTrigger.SET_ICONS_CLICK_INSTALL_SHORTCUT,
            AdsTrigger.SET_ICONS_CLICK_INSTALL_ALL_SHORTCUTS,
            AdsTrigger.SET_WALLPAPER_CLICK_HOME_SCREEN,
            AdsTrigger.SET_WALLPAPER_CLICK_LOCK_SCREEN,
            AdsTrigger.SET_WALLPAPER_CLICK_BOTH,
            AdsTrigger.ADD_WIDGETS_CLICK_UNLOCK,
        )) {
            val trigger = triggers.getJSONObject(id)
            assertTrue(id, trigger.getBoolean("enabled"))
            assertEquals(id, "inv_reward_apply", trigger.getString("inventory_id"))
            assertFalse(id, trigger.optBoolean("reward_bypass_on_failure"))
        }
    }

    @Test
    fun resumeExcludesFirstFlowAndTabNavigationHasNoInterstitial() {
        val triggers = config.getJSONObject("triggers")
        val condition = triggers.getJSONObject(AdsTrigger.APP_OPEN_ON_RESUME).getJSONObject("trigger")
        assertEquals("not_on_screens", condition.getString("type"))
        val excluded = condition.getJSONArray("not_on_screens")
        val screens = (0 until excluded.length()).map { excluded.getString(it) }.toSet()
        assertTrue(screens.containsAll(listOf(
            AdsScreen.SPLASH_VIEW_1, AdsScreen.SPLASH_VIEW_2,
            AdsScreen.LAN_VIEW, AdsScreen.LAN2_VIEW,
            AdsScreen.ONB_VIEW, AdsScreen.ONB1_VIEW, AdsScreen.ONB1_CLICK_VIEW,
            AdsScreen.ONB2_VIEW, AdsScreen.ONB3_VIEW,
            AdsScreen.WELCOME_VIEW, AdsScreen.WELCOME_BACK_VIEW,
        )))
        for (key in triggers.keys().asSequence().filter { it.startsWith("main_click_nav_") }) {
            assertFalse(key, triggers.getJSONObject(key).getBoolean("enabled"))
        }
        assertEquals("inv_inter_back", triggers.getJSONObject(AdsTrigger.RETURN_HOME).getString("inventory_id"))
    }
}
