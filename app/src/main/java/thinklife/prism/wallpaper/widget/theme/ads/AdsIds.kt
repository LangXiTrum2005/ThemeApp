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
    const val DISCARD_FEEDBACK_VIEW = "discard_feedback_view"
    const val DOWNLOAD_VIEW = "download_view"
    const val APPLY_ICONS_VIEW = "apply_icons_view"
    const val SET_ICONS_VIEW = "set_icons_view"
    const val ICONS_VIEW = "icons_view"
    const val GALLERY_VIEW = "gallery_view"
    const val SEARCH_VIEW = "search_view"
    const val APPLY_WALLPAPER_VIEW = "apply_wallpaper_view"
    const val PREVIEW_WALLPAPER_VIEW = "preview_wallpaper_view"
    const val WIDGETS_SUCCESS_VIEW = "widgets_success_view"
    const val SELECT_APP_VIEW = "select_app_view"
    const val GALLERY_THEMES_VIEW = "gallery_themes_view"
    const val GALLERY_WALLPAPERS_VIEW = "gallery_wallpapers_view"
    const val GALLERY_WIDGETS_VIEW = "gallery_widgets_view"
    const val WALKTHROUGH_VIEW = "walkthrough_view"
    const val WALKTHROUGH_INTRO_VIEW = "walkthrough_intro_view"
    const val WALKTHROUGH_DETAIL_VIEW = "walkthrough_detail_view"
    const val RATE_VIEW = "rate_view"
    const val FEEDBACK_VIEW = "feedback_view"
    const val APPLIED_THEME_VIEW = "applied_theme_view"
    const val DETAIL_BANNER_VIEW = "detail_banner_view"
    const val GET_THEME_VIEW = "get_theme_view"
    const val PREVIEW_THEME_VIEW = "preview_theme_view"
    const val THEME_CATEGORY_VIEW = "theme_category_view"
    const val SET_WALLPAPER_VIEW = "set_wallpaper_view"
    const val WALLPAPER_CATEGORY_VIEW = "wallpaper_category_view"
    const val ADD_WIDGETS_VIEW = "add_widgets_view"
    const val WIDGET_CATEGORY_VIEW = "widget_category_view"

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
    const val CONTROLLER_VIEW = "controller_view"
    const val CONTROLLER_PREVIEW_VIEW = "controller_preview_view"
    const val CONTROLLER_TUTORIAL_VIEW = "controller_tutorial_view"
    const val SETTINGS_VIEW = "settings_view"

    // Đổi ngôn ngữ đi từ Settings — màn RIÊNG, không dùng lại lan_view của luồng first-open.
    const val SETTING_LAN_VIEW = "setting_lan_view"
    const val WELCOME_BACK_VIEW = "welcome_back_view"
    const val NO_INTERNET_DIALOG_VIEW = "no_internet_dialog_view"
    const val PERMISSION_DIALOG_VIEW = "permission_dialog_view"
    // TODO(project): thêm hằng số cho MỌI màn mới của app và khai màn đó trong ads_config.json.
}

object AdsSlot {
    const val GALLERY_WIDGETS_SLOT_FEED = "gallery_widgets_slot_feed"
    const val GALLERY_WALLPAPERS_SLOT_FEED = "gallery_wallpapers_slot_feed"
    const val GALLERY_THEMES_SLOT_FEED = "gallery_themes_slot_feed"
    const val CONTROLLER_SLOT_FEED = "controller_slot_feed"
    const val ICONS_SLOT_FEED = "icons_slot_feed"
    const val WIDGET_CATEGORY_SLOT_FEED = "widget_category_slot_feed"
    const val WALLPAPER_CATEGORY_SLOT_FEED = "wallpaper_category_slot_feed"
    const val THEME_CATEGORY_SLOT_FEED = "theme_category_slot_feed"
    const val GALLERY_WIDGETS_SLOT_FEED_2 = "gallery_widgets_slot_feed_2"
    const val GALLERY_WIDGETS_SLOT_FEED_3 = "gallery_widgets_slot_feed_3"
    const val GALLERY_WALLPAPERS_SLOT_FEED_2 = "gallery_wallpapers_slot_feed_2"
    const val GALLERY_WALLPAPERS_SLOT_FEED_3 = "gallery_wallpapers_slot_feed_3"
    const val GALLERY_THEMES_SLOT_FEED_2 = "gallery_themes_slot_feed_2"
    const val GALLERY_THEMES_SLOT_FEED_3 = "gallery_themes_slot_feed_3"
    const val CONTROLLER_SLOT_FEED_2 = "controller_slot_feed_2"
    const val CONTROLLER_SLOT_FEED_3 = "controller_slot_feed_3"
    const val ICONS_SLOT_FEED_2 = "icons_slot_feed_2"
    const val ICONS_SLOT_FEED_3 = "icons_slot_feed_3"
    const val WIDGET_CATEGORY_SLOT_FEED_2 = "widget_category_slot_feed_2"
    const val WIDGET_CATEGORY_SLOT_FEED_3 = "widget_category_slot_feed_3"
    const val WALLPAPER_CATEGORY_SLOT_FEED_2 = "wallpaper_category_slot_feed_2"
    const val WALLPAPER_CATEGORY_SLOT_FEED_3 = "wallpaper_category_slot_feed_3"
    const val THEME_CATEGORY_SLOT_FEED_2 = "theme_category_slot_feed_2"
    const val THEME_CATEGORY_SLOT_FEED_3 = "theme_category_slot_feed_3"

    val feedSlotGroups: List<List<String>>
        get() = listOf(
            GALLERY_WIDGETS_SLOT_FEED, GALLERY_WALLPAPERS_SLOT_FEED,
            GALLERY_THEMES_SLOT_FEED, CONTROLLER_SLOT_FEED, ICONS_SLOT_FEED,
            WIDGET_CATEGORY_SLOT_FEED, WALLPAPER_CATEGORY_SLOT_FEED, THEME_CATEGORY_SLOT_FEED,
        ).map(::feedSlots)

    // SDK giữ một container trên mỗi ID; các trang category cũng dùng chung pool này.
    fun feedSlots(slotId: String): List<String> = when (slotId) {
        GALLERY_WIDGETS_SLOT_FEED -> listOf(
            GALLERY_WIDGETS_SLOT_FEED,
            GALLERY_WIDGETS_SLOT_FEED_2,
            GALLERY_WIDGETS_SLOT_FEED_3,
        )
        GALLERY_WALLPAPERS_SLOT_FEED -> listOf(
            GALLERY_WALLPAPERS_SLOT_FEED,
            GALLERY_WALLPAPERS_SLOT_FEED_2,
            GALLERY_WALLPAPERS_SLOT_FEED_3,
        )
        GALLERY_THEMES_SLOT_FEED -> listOf(
            GALLERY_THEMES_SLOT_FEED,
            GALLERY_THEMES_SLOT_FEED_2,
            GALLERY_THEMES_SLOT_FEED_3,
        )
        CONTROLLER_SLOT_FEED -> listOf(
            CONTROLLER_SLOT_FEED,
            CONTROLLER_SLOT_FEED_2,
            CONTROLLER_SLOT_FEED_3,
        )
        ICONS_SLOT_FEED -> listOf(
            ICONS_SLOT_FEED,
            ICONS_SLOT_FEED_2,
            ICONS_SLOT_FEED_3,
        )
        WIDGET_CATEGORY_SLOT_FEED -> listOf(
            WIDGET_CATEGORY_SLOT_FEED,
            WIDGET_CATEGORY_SLOT_FEED_2,
            WIDGET_CATEGORY_SLOT_FEED_3,
        )
        WALLPAPER_CATEGORY_SLOT_FEED -> listOf(
            WALLPAPER_CATEGORY_SLOT_FEED,
            WALLPAPER_CATEGORY_SLOT_FEED_2,
            WALLPAPER_CATEGORY_SLOT_FEED_3,
        )
        THEME_CATEGORY_SLOT_FEED -> listOf(
            THEME_CATEGORY_SLOT_FEED,
            THEME_CATEGORY_SLOT_FEED_2,
            THEME_CATEGORY_SLOT_FEED_3,
        )
        else -> listOf(slotId)
    }

    fun tag(slotId: String) = "ads_slot:$slotId"
    fun listTag(slotId: String) = "list_ads_slot:$slotId"

    const val DISCARD_FEEDBACK_SLOT_TOP = "discard_feedback_slot_top"
    const val DISCARD_FEEDBACK_SLOT_BOTTOM = "discard_feedback_slot_bottom"
    const val DOWNLOAD_SLOT_TOP = "download_slot_top"
    const val DOWNLOAD_SLOT_BOTTOM = "download_slot_bottom"
    const val APPLY_ICONS_SLOT_TOP = "apply_icons_slot_top"
    const val APPLY_ICONS_SLOT_BOTTOM = "apply_icons_slot_bottom"
    const val SET_ICONS_SLOT_TOP = "set_icons_slot_top"
    const val SET_ICONS_SLOT_BOTTOM = "set_icons_slot_bottom"
    const val ICONS_SLOT_TOP = "icons_slot_top"
    const val ICONS_SLOT_BOTTOM = "icons_slot_bottom"
    const val GALLERY_SLOT_TOP = "gallery_slot_top"
    const val GALLERY_SLOT_BOTTOM = "gallery_slot_bottom"
    const val SEARCH_SLOT_TOP = "search_slot_top"
    const val SEARCH_SLOT_BOTTOM = "search_slot_bottom"
    const val APPLY_WALLPAPER_SLOT_TOP = "apply_wallpaper_slot_top"
    const val APPLY_WALLPAPER_SLOT_BOTTOM = "apply_wallpaper_slot_bottom"
    const val PREVIEW_WALLPAPER_SLOT_TOP = "preview_wallpaper_slot_top"
    const val PREVIEW_WALLPAPER_SLOT_BOTTOM = "preview_wallpaper_slot_bottom"
    const val WIDGETS_SUCCESS_SLOT_TOP = "widgets_success_slot_top"
    const val WIDGETS_SUCCESS_SLOT_BOTTOM = "widgets_success_slot_bottom"
    const val SELECT_APP_SLOT_TOP = "select_app_slot_top"
    const val SELECT_APP_SLOT_BOTTOM = "select_app_slot_bottom"
    const val GALLERY_THEMES_SLOT_TOP = "gallery_themes_slot_top"
    const val GALLERY_THEMES_SLOT_BOTTOM = "gallery_themes_slot_bottom"
    const val GALLERY_WALLPAPERS_SLOT_TOP = "gallery_wallpapers_slot_top"
    const val GALLERY_WALLPAPERS_SLOT_BOTTOM = "gallery_wallpapers_slot_bottom"
    const val GALLERY_WIDGETS_SLOT_TOP = "gallery_widgets_slot_top"
    const val GALLERY_WIDGETS_SLOT_BOTTOM = "gallery_widgets_slot_bottom"
    const val WALKTHROUGH_SLOT_TOP = "walkthrough_slot_top"
    const val WALKTHROUGH_SLOT_BOTTOM = "walkthrough_slot_bottom"
    const val WALKTHROUGH_INTRO_SLOT_TOP = "walkthrough_intro_slot_top"
    const val WALKTHROUGH_INTRO_SLOT_BOTTOM = "walkthrough_intro_slot_bottom"
    const val WALKTHROUGH_DETAIL_SLOT_TOP = "walkthrough_detail_slot_top"
    const val WALKTHROUGH_DETAIL_SLOT_BOTTOM = "walkthrough_detail_slot_bottom"
    const val RATE_SLOT_TOP = "rate_slot_top"
    const val RATE_SLOT_BOTTOM = "rate_slot_bottom"
    const val FEEDBACK_SLOT_TOP = "feedback_slot_top"
    const val FEEDBACK_SLOT_BOTTOM = "feedback_slot_bottom"
    const val APPLIED_THEME_SLOT_TOP = "applied_theme_slot_top"
    const val APPLIED_THEME_SLOT_BOTTOM = "applied_theme_slot_bottom"
    const val DETAIL_BANNER_SLOT_TOP = "detail_banner_slot_top"
    const val DETAIL_BANNER_SLOT_BOTTOM = "detail_banner_slot_bottom"
    const val GET_THEME_SLOT_TOP = "get_theme_slot_top"
    const val GET_THEME_SLOT_BOTTOM = "get_theme_slot_bottom"
    const val PREVIEW_THEME_SLOT_TOP = "preview_theme_slot_top"
    const val PREVIEW_THEME_SLOT_BOTTOM = "preview_theme_slot_bottom"
    const val THEME_CATEGORY_SLOT_TOP = "theme_category_slot_top"
    const val THEME_CATEGORY_SLOT_BOTTOM = "theme_category_slot_bottom"
    const val SET_WALLPAPER_SLOT_TOP = "set_wallpaper_slot_top"
    const val SET_WALLPAPER_SLOT_BOTTOM = "set_wallpaper_slot_bottom"
    const val WALLPAPER_CATEGORY_SLOT_TOP = "wallpaper_category_slot_top"
    const val WALLPAPER_CATEGORY_SLOT_BOTTOM = "wallpaper_category_slot_bottom"
    const val ADD_WIDGETS_SLOT_TOP = "add_widgets_slot_top"
    const val ADD_WIDGETS_SLOT_BOTTOM = "add_widgets_slot_bottom"
    const val WIDGET_CATEGORY_SLOT_TOP = "widget_category_slot_top"
    const val WIDGET_CATEGORY_SLOT_BOTTOM = "widget_category_slot_bottom"

    const val CONTROLLER_PREVIEW_SLOT_TOP = "controller_preview_slot_top"
    const val CONTROLLER_PREVIEW_SLOT_START = "controller_preview_slot_start"
    const val CONTROLLER_PREVIEW_SLOT_END = "controller_preview_slot_end"
    const val CONTROLLER_PREVIEW_SLOT_BOTTOM = "controller_preview_slot_bottom"

    const val CONTROLLER_SLOT_TOP = "controller_slot_top"
    const val CONTROLLER_SLOT_START = "controller_slot_start"
    const val CONTROLLER_SLOT_END = "controller_slot_end"
    const val CONTROLLER_SLOT_BOTTOM = "controller_slot_bottom"
    const val CONTROLLER_SLOT_1ST_ITEM = "controller_slot_1st_item"
    const val CONTROLLER_SLOT_3RD_ITEM = "controller_slot_3rd_item"
    const val CONTROLLER_TUTORIAL_SLOT_TOP = "controller_tutorial_slot_top"
    const val CONTROLLER_TUTORIAL_SLOT_START = "controller_tutorial_slot_start"
    const val CONTROLLER_TUTORIAL_SLOT_END = "controller_tutorial_slot_end"
    const val CONTROLLER_TUTORIAL_SLOT_BOTTOM = "controller_tutorial_slot_bottom"

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
    const val FEEDBACK_CLICK_REMOVE_IMAGE = "feedback_click_remove_image"
    const val MAIN_CLICK_BACK = "main_click_back"
    const val FEEDBACK_CLICK_BACK = "feedback_click_back"
    const val PREVIEW_WALLPAPER_CLICK_BACK = "preview_wallpaper_click_back"
    const val MAIN_CLICK_NAV_GALLERY = "main_click_nav_gallery"
    const val MAIN_CLICK_NAV_ICONS = "main_click_nav_icons"
    const val MAIN_CLICK_NAV_CONTROLLER = "main_click_nav_controller"
    const val GET_THEME_CLICK_START_WALLPAPER_DOWNLOAD = "get_theme_click_start_wallpaper_download"
    const val GET_THEME_CLICK_INSTALL_ALL_SHORTCUTS = "get_theme_click_install_all_shortcuts"
    const val GET_THEME_CLICK_INSTALL_SHORTCUT = "get_theme_click_install_shortcut"
    const val SET_ICONS_CLICK_INSTALL_ALL_SHORTCUTS = "set_icons_click_install_all_shortcuts"
    const val SET_ICONS_CLICK_INSTALL_SHORTCUT = "set_icons_click_install_shortcut"
    const val RETURN_HOME = "return_home"
    const val DISCARD_FEEDBACK_CLICK_DISCARD = "discard_feedback_click_discard"
    const val DISCARD_FEEDBACK_CLICK_KEEP_EDITING = "discard_feedback_click_keep_editing"
    const val APPLY_ICONS_CLICK_HOME = "apply_icons_click_home"
    const val APPLY_ICONS_CLICK_MORE_ICONS = "apply_icons_click_more_icons"
    const val APPLY_ICONS_CLICK_BACK = "apply_icons_click_back"
    const val SET_ICONS_CLICK_INSTALL_ALL = "set_icons_click_install_all"
    const val SET_ICONS_CLICK_ADAPTER_ACTION_CLICK = "set_icons_click_adapter_action_click"
    const val SET_ICONS_CLICK_ADAPTER_EDIT_CLICK = "set_icons_click_adapter_edit_click"
    const val SET_ICONS_CLICK_BACK = "set_icons_click_back"
    const val ICONS_CLICK_SETTING = "icons_click_setting"
    const val ICONS_CLICK_MY_GALLERY = "icons_click_my_gallery"
    const val ICONS_CLICK_ED_SEARCH = "icons_click_ed_search"
    const val ICONS_CLICK_ERROR_RETRY = "icons_click_error_retry"
    const val ICONS_CLICK_ICON_ADAPTER_CLICK_ITEM = "icons_click_icon_adapter_click_item"
    const val SETTING_LAN_CLICK_CLEAR_SEARCH = "setting_lan_click_clear_search"
    const val GALLERY_CLICK_SETTING = "gallery_click_setting"
    const val GALLERY_CLICK_MY_GALLERY = "gallery_click_my_gallery"
    const val SEARCH_CLICK_ERROR_RETRY = "search_click_error_retry"
    const val SEARCH_CLICK_POPULAR_ADAPTER_CLICK_ITEM = "search_click_popular_adapter_click_item"
    const val SEARCH_CLICK_THEME_ADAPTER_CLICK_ITEM = "search_click_theme_adapter_click_item"
    const val SEARCH_CLICK_WALLPAPER_ADAPTER_CLICK_ITEM = "search_click_wallpaper_adapter_click_item"
    const val SEARCH_CLICK_ICON_ADAPTER_CLICK_ITEM = "search_click_icon_adapter_click_item"
    const val SEARCH_CLICK_WIDGET_ADAPTER_CLICK_ITEM = "search_click_widget_adapter_click_item"
    const val SEARCH_CLICK_BACK = "search_click_back"
    const val SETTINGS_CLICK_PRIVACY = "settings_click_privacy"
    const val SETTINGS_CLICK_FEEDBACK = "settings_click_feedback"
    const val SETTINGS_CLICK_BACK = "settings_click_back"
    const val HOME_CLICK_SETTING = "home_click_setting"
    const val HOME_CLICK_MY_GALLERY = "home_click_my_gallery"
    const val HOME_CLICK_ED_SEARCH = "home_click_ed_search"
    const val HOME_CLICK_BANNER_ADAPTER_CLICK_ITEM = "home_click_banner_adapter_click_item"
    const val HOME_CLICK_ERROR_RETRY = "home_click_error_retry"
    const val HOME_CLICK_MORE_CATEGORY = "home_click_more_category"
    const val HOME_CLICK_RETRY = "home_click_retry"
    const val APPLY_WALLPAPER_CLICK_WALLPAPER_ADAPTER_CLICK_ITEM = "apply_wallpaper_click_wallpaper_adapter_click_item"
    const val APPLY_WALLPAPER_CLICK_HOME = "apply_wallpaper_click_home"
    const val APPLY_WALLPAPER_CLICK_BACK = "apply_wallpaper_click_back"
    const val PREVIEW_WALLPAPER_CLICK_DOWNLOAD = "preview_wallpaper_click_download"
    const val PREVIEW_WALLPAPER_CLICK_PREVIEW = "preview_wallpaper_click_preview"
    const val PREVIEW_WALLPAPER_CLICK_APPLY = "preview_wallpaper_click_apply"
    const val TAB2_CLICK_SETTING = "tab2_click_setting"
    const val TAB2_CLICK_MY_GALLERY = "tab2_click_my_gallery"
    const val TAB2_CLICK_ED_SEARCH = "tab2_click_ed_search"
    const val TAB2_CLICK_ERROR_RETRY = "tab2_click_error_retry"
    const val TAB2_CLICK_MORE_CATEGORY = "tab2_click_more_category"
    const val TAB3_CLICK_SETTING = "tab3_click_setting"
    const val TAB3_CLICK_MY_GALLERY = "tab3_click_my_gallery"
    const val TAB3_CLICK_ED_SEARCH = "tab3_click_ed_search"
    const val TAB3_CLICK_ERROR_RETRY = "tab3_click_error_retry"
    const val TAB3_CLICK_MORE_CATEGORY = "tab3_click_more_category"
    const val WIDGETS_SUCCESS_CLICK_BACK = "widgets_success_click_back"
    const val WIDGETS_SUCCESS_CLICK_HOME = "widgets_success_click_home"
    const val WIDGETS_SUCCESS_CLICK_CATEGORY_ADAPTER_CLICK_ITEM = "widgets_success_click_category_adapter_click_item"
    const val WIDGETS_SUCCESS_CLICK_SIZE_ADAPTER_CLICK_ITEM = "widgets_success_click_size_adapter_click_item"
    const val SELECT_APP_CLICK_CLOSE = "select_app_click_close"
    const val SELECT_APP_CLICK_ADAPTER_CLICK_ITEM = "select_app_click_adapter_click_item"
    const val GALLERY_THEMES_CLICK_ADAPTER_CLICK_ITEM = "gallery_themes_click_adapter_click_item"
    const val GALLERY_WALLPAPERS_CLICK_ADAPTER_CLICK_ITEM = "gallery_wallpapers_click_adapter_click_item"
    const val GALLERY_WIDGETS_CLICK_ADAPTER_CLICK_ITEM = "gallery_widgets_click_adapter_click_item"
    const val WALKTHROUGH_CLICK_IV_CLOSE = "walkthrough_click_iv_close"
    const val WALKTHROUGH_CLICK_BACK = "walkthrough_click_back"
    const val WALKTHROUGH_INTRO_CLICK_NEXT = "walkthrough_intro_click_next"
    const val WALKTHROUGH_DETAIL_CLICK_NEXT = "walkthrough_detail_click_next"
    const val RATE_CLICK_CLOSE = "rate_click_close"
    const val RATE_CLICK_RATE = "rate_click_rate"
    const val FEEDBACK_CLICK_SEND_FEEDBACK = "feedback_click_send_feedback"
    const val FEEDBACK_CLICK_ADD_IMAGE = "feedback_click_add_image"
    const val APPLIED_THEME_CLICK_HOME = "applied_theme_click_home"
    const val APPLIED_THEME_CLICK_MORE_ICONS = "applied_theme_click_more_icons"
    const val APPLIED_THEME_CLICK_BACK = "applied_theme_click_back"
    const val DETAIL_BANNER_CLICK_THEME_ADAPTER_CLICK_ITEM = "detail_banner_click_theme_adapter_click_item"
    const val DETAIL_BANNER_CLICK_ERROR_RETRY = "detail_banner_click_error_retry"
    const val DETAIL_BANNER_CLICK_BACK = "detail_banner_click_back"
    const val GET_THEME_CLICK_TAB_WALLPAPERS = "get_theme_click_tab_wallpapers"
    const val GET_THEME_CLICK_TAB_ICONS = "get_theme_click_tab_icons"
    const val GET_THEME_CLICK_TAB_WIDGETS = "get_theme_click_tab_widgets"
    const val GET_THEME_CLICK_FRAME_WIDGET_SMALL = "get_theme_click_frame_widget_small"
    const val GET_THEME_CLICK_FRAME_WIDGET_MEDIUM = "get_theme_click_frame_widget_medium"
    const val GET_THEME_CLICK_FRAME_WIDGET_LARGE = "get_theme_click_frame_widget_large"
    const val GET_THEME_CLICK_APPLY_WALLPAPER = "get_theme_click_apply_wallpaper"
    const val GET_THEME_CLICK_INSTALL_ALL = "get_theme_click_install_all"
    const val GET_THEME_CLICK_GO_TO_HOME = "get_theme_click_go_to_home"
    const val GET_THEME_CLICK_ICON_ADAPTER_ACTION_CLICK = "get_theme_click_icon_adapter_action_click"
    const val GET_THEME_CLICK_ICON_ADAPTER_EDIT_CLICK = "get_theme_click_icon_adapter_edit_click"
    const val GET_THEME_CLICK_BACK = "get_theme_click_back"
    const val PREVIEW_THEME_CLICK_GET_THEMES = "preview_theme_click_get_themes"
    const val PREVIEW_THEME_CLICK_BACK = "preview_theme_click_back"
    const val THEME_CATEGORY_CLICK_THEME_ADAPTER_CLICK_ITEM = "theme_category_click_theme_adapter_click_item"
    const val THEME_CATEGORY_CLICK_ERROR_RETRY = "theme_category_click_error_retry"
    const val SET_WALLPAPER_CLICK_CLOSE = "set_wallpaper_click_close"
    const val SET_WALLPAPER_CLICK_HOME_SCREEN = "set_wallpaper_click_home_screen"
    const val SET_WALLPAPER_CLICK_LOCK_SCREEN = "set_wallpaper_click_lock_screen"
    const val SET_WALLPAPER_CLICK_BOTH = "set_wallpaper_click_both"
    const val WALLPAPER_CATEGORY_CLICK_WALLPAPER_ADAPTER_CLICK_ITEM = "wallpaper_category_click_wallpaper_adapter_click_item"
    const val WALLPAPER_CATEGORY_CLICK_ERROR_RETRY = "wallpaper_category_click_error_retry"
    const val ADD_WIDGETS_CLICK_CLOSE = "add_widgets_click_close"
    const val ADD_WIDGETS_CLICK_UNLOCK = "add_widgets_click_unlock"
    const val WIDGET_CATEGORY_CLICK_WIDGET_ADAPTER_CLICK_ITEM = "widget_category_click_widget_adapter_click_item"
    const val WIDGET_CATEGORY_CLICK_ERROR_RETRY = "widget_category_click_error_retry"

    const val CONTROLLER_PREVIEW_CLICK_ACTION = "controller_preview_click_action"
    const val CONTROLLER_PREVIEW_CLICK_BACK = "controller_preview_click_back"
    const val CONTROLLER_PREVIEW_CLICK_RETRY = "controller_preview_click_retry"

    const val CONTROLLER_CLICK_ENABLE = "controller_click_enable"
    const val CONTROLLER_CLICK_THEME = "controller_click_theme"
    const val CONTROLLER_CLICK_SETTINGS = "controller_click_settings"
    const val CONTROLLER_CLICK_GALLERY = "controller_click_gallery"
    const val CONTROLLER_TUTORIAL_CLICK_ENABLE = "controller_tutorial_click_enable"
    const val CONTROLLER_TUTORIAL_CLICK_BACK = "controller_tutorial_click_back"

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
    const val NATIVE_HOME = "inv_native_home"
    const val NATIVE_HOME_2 = "inv_native_home_2"
    const val NATIVE_HOME_3 = "inv_native_home_3"

    val nativeFeedPool: List<String>
        get() = listOf(NATIVE_HOME, NATIVE_HOME_2, NATIVE_HOME_3)
}
