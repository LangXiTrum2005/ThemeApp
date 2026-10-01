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

/**
 * ID khớp 100% với `assets/ads_config.json` — đây là hợp đồng giữa code và config.
 * Sửa file này phải sửa file kia (và ngược lại).
 */
object AdsScreen {
    // Liệt kê ĐỦ FULL tất cả các màn của app, kể cả màn không có ads: PreloadEngine.currentScreenId
    // chỉ được set khi màn bind/log — trigger(), safety_rules, not_on_screens, resume-ad preload
    // và funnel tracking đều dựa vào nó.
    const val SPLASH_VIEW_1 = "splash_view_1" // phiên đầu
    const val SPLASH_VIEW_2 = "splash_view_2" // phiên sau
    const val LAN_VIEW = "lan_view" // LanguageActivity — trạng thái 1 (chưa chọn)
    const val LAN2_VIEW = "lan2_view" // LanguageActivity — trạng thái 2 (đã chọn)
    const val ONB_VIEW = "onb_view" // OnboardingActivity (container pager)
    const val ONB1_VIEW = "onb1_view" // SurveyFragment — trạng thái 1
    const val ONB1_CLICK_VIEW = "onb1_click_view" // SurveyFragment — sau khi chọn option đầu tiên
    const val ONB2_VIEW = "onb2_view"
    const val ONB3_VIEW = "onb3_view"
    const val WELCOME_VIEW = "welcome_view" // WelcomeFragment (luồng first open)
    const val MAIN_VIEW = "main_view" // MainActivity (container + banner đáy)
    const val HOME_VIEW = "home_view"
    const val TAB2_VIEW = "tab2_view"
    const val TAB3_VIEW = "tab3_view"
    const val SETTINGS_VIEW = "settings_view"

    // Đổi ngôn ngữ đi từ Settings — màn RIÊNG, không dùng lại lan_view của luồng first-open.
    const val SETTING_LAN_VIEW = "setting_lan_view"
    const val WELCOME_BACK_VIEW = "welcome_back_view"
    const val NO_INTERNET_DIALOG_VIEW = "no_internet_dialog_view"
    const val PERMISSION_DIALOG_VIEW = "permission_dialog_view"
    // TODO(project): thêm hằng số cho MỌI màn mới của app và khai màn đó trong ads_config.json.
}

object AdsSlot {
    // BỘ SLOT CHUẨN mỗi màn:
    //   <màn>_slot_top / _bottom  — neo cứng ngoài vùng scroll (LUÔN có ở mọi màn)
    //   <màn>_slot_start / _end   — đầu / cuối content cuộn
    //   <màn>_slot_1st_item / _3rd_item — ad trong danh sách. Khai trong config + hằng số nhưng
    //     chưa nối adapter (config đang enabled:false); bật thật cần thêm tag
    //     "list_ads_slot:<slot_id>" ở item layout + AdsSlotScanner.processSingleSlot ở onBind.
    const val SPLASH_SLOT_TOP = "splash_slot_top"
    const val SPLASH_SLOT_BOTTOM = "splash_slot_bottom"

    const val LAN_SLOT_TOP = "lan_slot_top"
    const val LAN_SLOT_END = "lan_slot_end"
    const val LAN_SLOT_BOTTOM = "lan_slot_bottom"
    const val LAN_SLOT_1ST_ITEM = "lan_slot_1st_item"
    const val LAN_SLOT_3RD_ITEM = "lan_slot_3rd_item"
    const val LAN2_SLOT_TOP = "lan2_slot_top"
    const val LAN2_SLOT_END = "lan2_slot_end"
    const val LAN2_SLOT_BOTTOM = "lan2_slot_bottom"

    const val ONB_SLOT_TOP = "onb_slot_top"
    const val ONB_SLOT_BOTTOM = "onb_slot_bottom"
    const val ONB1_SLOT_TOP = "onb1_slot_top"
    const val ONB1_SLOT_END = "onb1_slot_end"
    const val ONB1_SLOT_BOTTOM = "onb1_slot_bottom"
    const val ONB1_SLOT_1ST_ITEM = "onb1_slot_1st_item"
    const val ONB1_SLOT_3RD_ITEM = "onb1_slot_3rd_item"
    const val ONB1_CLICK_SLOT_TOP = "onb1_click_slot_top"
    const val ONB1_CLICK_SLOT_END = "onb1_click_slot_end"
    const val ONB1_CLICK_SLOT_BOTTOM = "onb1_click_slot_bottom"
    const val ONB2_SLOT_TOP = "onb2_slot_top"
    const val ONB2_SLOT_END = "onb2_slot_end"
    const val ONB2_SLOT_BOTTOM = "onb2_slot_bottom"
    const val ONB3_SLOT_TOP = "onb3_slot_top"
    const val ONB3_SLOT_END = "onb3_slot_end"
    const val ONB3_SLOT_BOTTOM = "onb3_slot_bottom"

    const val WELCOME_SLOT_TOP = "welcome_slot_top"
    const val WELCOME_SLOT_END = "welcome_slot_end"
    const val WELCOME_SLOT_BOTTOM = "welcome_slot_bottom"

    const val MAIN_SLOT_TOP = "main_slot_top"
    const val MAIN_SLOT_BOTTOM = "main_slot_bottom"

    const val HOME_SLOT_TOP = "home_slot_top"
    const val HOME_SLOT_START = "home_slot_start"
    const val HOME_SLOT_END = "home_slot_end"
    const val HOME_SLOT_BOTTOM = "home_slot_bottom"

    const val TAB2_SLOT_TOP = "tab2_slot_top"
    const val TAB2_SLOT_START = "tab2_slot_start"
    const val TAB2_SLOT_END = "tab2_slot_end"
    const val TAB2_SLOT_BOTTOM = "tab2_slot_bottom"

    const val TAB3_SLOT_TOP = "tab3_slot_top"
    const val TAB3_SLOT_START = "tab3_slot_start"
    const val TAB3_SLOT_END = "tab3_slot_end"
    const val TAB3_SLOT_BOTTOM = "tab3_slot_bottom"

    const val SETTINGS_SLOT_TOP = "settings_slot_top"
    const val SETTINGS_SLOT_START = "settings_slot_start"
    const val SETTINGS_SLOT_END = "settings_slot_end"
    const val SETTINGS_SLOT_BOTTOM = "settings_slot_bottom"

    const val SETTING_LAN_SLOT_TOP = "setting_lan_slot_top"
    const val SETTING_LAN_SLOT_END = "setting_lan_slot_end"
    const val SETTING_LAN_SLOT_BOTTOM = "setting_lan_slot_bottom"
    const val SETTING_LAN_SLOT_1ST_ITEM = "setting_lan_slot_1st_item"
    const val SETTING_LAN_SLOT_3RD_ITEM = "setting_lan_slot_3rd_item"

    const val WELCOME_BACK_SLOT_TOP = "welcome_back_slot_top"
    const val WELCOME_BACK_SLOT_BOTTOM = "welcome_back_slot_bottom"

    const val NO_INTERNET_DIALOG_SLOT_TOP = "no_internet_dialog_slot_top"
    const val NO_INTERNET_DIALOG_SLOT_BOTTOM = "no_internet_dialog_slot_bottom"

    const val PERMISSION_DIALOG_SLOT_TOP = "permission_dialog_slot_top"
    const val PERMISSION_DIALOG_SLOT_BOTTOM = "permission_dialog_slot_bottom"
}

object AdsTrigger {
    // TOÀN BỘ nút bấm trong app đều đi qua triggerAds() với trigger khai ở config.
    // Nút chưa có plan ads → config để enabled:false + inventory_id:"" (đếm ngầm,
    // AdOps bật sau bằng Remote Config, không cần build lại app).
    const val SPLASH_LEAVE_1 = "splash_leave_1"
    const val SPLASH_LEAVE_2 = "splash_leave_2"

    const val LAN_CLICK_ITEM = "lan_click_item"
    const val LAN_CLICK_DONE = "lan_click_done"

    const val ONB1_CLICK_OPTION = "onb1_click_option"
    const val ONB1_CLICK_NEXT = "onb1_click_next"
    const val ONB2_CLICK_NEXT = "onb2_click_next"
    const val ONB3_CLICK_NEXT = "onb3_click_next"
    const val WELCOME_CLICK_NEXT = "welcome_click_next"

    const val MAIN_CLICK_NAV_HOME = "main_click_nav_home"
    const val MAIN_CLICK_NAV_TAB2 = "main_click_nav_tab2"
    const val MAIN_CLICK_NAV_TAB3 = "main_click_nav_tab3"
    const val MAIN_CLICK_NAV_SETTINGS = "main_click_nav_settings"

    const val SETTINGS_CLICK_LANGUAGE = "settings_click_language"
    const val SETTINGS_CLICK_RATE = "settings_click_rate"

    const val SETTING_LAN_CLICK_ITEM = "setting_lan_click_item"
    const val SETTING_LAN_CLICK_BACK = "setting_lan_click_back"
    const val SETTING_LAN_CLICK_SAVE = "setting_lan_click_save"

    const val WELCOME_BACK_CLICK_CONTINUE = "welcome_back_click_continue"

    const val NO_INTERNET_CLICK_TRY_AGAIN = "no_internet_click_try_again"
    const val NO_INTERNET_CLICK_CLOSE = "no_internet_click_close"

    const val PERMISSION_CLICK_ALLOW = "permission_click_allow"
    const val PERMISSION_CLICK_DENY = "permission_click_deny"

    /** 2 trigger cấp SDK — AppOpenManager tự bắn, không gắn vào nút nào. */
    const val APP_OPEN = "app_open"
    const val APP_OPEN_ON_RESUME = "app_open_on_resume"
}

/** Chỉ khai inventory mà code cần đợi/preload đích danh. */
object AdsInventory {
    const val NATIVE_LFO1 = "inv_native_lfo1"
}
