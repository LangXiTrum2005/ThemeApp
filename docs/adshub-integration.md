# AdsHub integration — Home và các màn sau Home

## Mapping theo brief

| Placement | Màn / thao tác thực tế | Cách gắn |
| --- | --- | --- |
| banner_splash, inter_splash, inter_splash_2nd | LaunchActivity | Giữ luồng hiện có; cập nhật App ID và các ID high/all theo brief. |
| native_lfo1, native_lfo2 | Hai trạng thái của LanguageActivity | Giữ chuyển slot thủ công hiện có. |
| native_onb1, native_onb1_click | Hai trạng thái của SurveyFragment | Giữ chuyển slot thủ công hiện có. |
| native_full1, native_full2 | Sau trang onboarding 2 và 3 | Trigger fullscreen hiện có. |
| native_welcome | WelcomeFragment | Native hiện có. |
| native_resume | SDK app_open_on_resume, layout_native_resume | Loại trừ splash, language, onboarding, walkthrough, welcome và welcome-back; giữ skip_resume_ad_after_click. |
| native_home | ThemeCategoryFragment, WallpaperCategoryFragment, WidgetCategoryFragment, IconFragment, ControllerFragment, các trang MyGallery | NativeFeedAdapter chèn native_small sau mỗi 6 nội dung; ad chiếm trọn hàng trong grid. |
| banner_home | MainActivity | Slot đáy nằm ngoài pager và thanh điều hướng, không đè nội dung. |
| inter_back | Back về MainActivity hoặc nút Home ở màn kết quả | Theo dõi màn xuất phát bằng Intent extra. Back giữa các màn con và chuyển tab không tự bật inter. |
| native_inapp | Preview/Get/Applied Theme, DetailBanner, Preview/Apply Wallpaper, Set/Apply Icons, WidgetsSuccess, Search, Settings, SettingLanguage, Feedback, ControllerPreview/Tutorial, các sheet chọn/cài nội dung | Slot đáy native_small. Các dialog trạng thái ngắn (download, rate, discard, permission, mất mạng) chỉ có slot và tracking dự phòng. |
| reward_apply | Tải wallpaper; cài wallpaper lên Home/Lock/Both; cài từng icon hoặc tất cả icon trong theme/icon pack; Add Widget; tải/áp dụng controller theme | Load on demand, high → all, không preload/reload; hành động chỉ chạy sau callback nhận thưởng và đóng ad. |

Native theo chu kỳ 6 là yêu cầu cập nhật để ads xuất hiện sau 2 hàng của grid 3 cột, sử dụng slot `*_slot_feed`; không bật các slot đầu danh sách/sau item 3 của template.

## Sửa native list bị loading mãi

- Tham khảo `ta079-keyboard-emoji`: SDK chỉ giữ một container cho mỗi slot ID và một cache trên mỗi inventory. Mỗi feed có 3 slot (`*_slot_feed`, `_2`, `_3`) trỏ vào 3 inventory riêng (`inv_native_home`, `_2`, `_3`), cùng waterfall/AdMob ID. Chỉ chọn slot và inventory chưa có container khác đang dùng.
- Ba inventory preload khi vào màn list, `sticky: true`, `cache_ttl_sec: 1800`, `reload_after_render: true`. Holder giữ view ads khi detach tạm; pause/recycle/destroy gỡ container để tab khác không bind trùng ad. SDK giữ cache nên quay lại tab có thể render ngay. Không rebind ads khi chỉ đổi selection của nội dung.
- Debounce attach 200ms để hạn chế request lúc fling. Config/slot chỉ chờ hữu hạn; skeleton chờ `inventory.timeout_ms + 2000ms` (hiện tại 18 giây) rồi thu gọn nếu chưa render. Không coi việc gửi request là render thành công.
- Mỗi lần tải có container con riêng. Callback đến sau timeout/recycle không thể gắn skeleton vào row đang dùng cho lần tải mới. Request thất bại không tự retry liên tục; retry khi row được attach lại hoặc quay lại tab.
- `NativeFeedConfig.kt` bổ sung slot và inventory còn thiếu trong JSON Firebase cũ trước khi đưa vào SDK; giữ AdMob ID, waterfall, điều kiện và flag tắt ads. Bản cũ dùng cache chung được chuyển sang preload/sticky với 3 inventory; các override khác của slot đã có được giữ lại. Assets tăng `config_version` lên 6.

## File thay đổi

- `app/src/main/assets/ads_config.json`: đủ 30 ID, waterfall high/all, inventory reward/back/inapp, style native_small và bản đồ màn/slot/trigger.
- `app/src/main/AndroidManifest.xml`: App ID thật trong brief.
- `ads/AdsIds.kt`, `ads/AdsBootstrap.kt`, `ads/NativeFeedAdapter.kt`: hợp đồng ID, callback reward và vòng đời native trong RecyclerView.
- Các base Activity/Fragment/Dialog/BottomSheet: chống bấm reward lặp, đợi RESUMED, bỏ callback nếu view đã hủy; giữ screen ID đúng khi quay lại màn.
- MainTab/MainActivity, các màn Home, chi tiết, kết quả, cài đặt và layout tương ứng: slot, trigger và adapter ads.
- `res/layout/item_native_feed.xml`, `res/values/strings.xml`, `res/values/dimens.xml`: khung native và resource cần thiết.
- `ads/NativeFeedConfig.kt`, `src/test/.../ads/AdsConfigTest.kt`, `NativeFeedAdapterTest.kt`: tương thích Remote Config cũ, pool không trùng slot/inventory, cadence sau 6 item, SDK parse JSON, hợp đồng ID, reward và resume/back routing.

Giữ nguyên SDK `com.onetabb:adshub:0.0.1`, không thêm dependency. Không sửa secret, workflow, wrapper hoặc version catalog. Không thay đổi thiết kế icon đang được sửa trước đó.

## Kiểm chứng

- `:app:assembleDebug` và `:app:testDebugUnitTest`.
- Kiểm cơ học: `screens OK: 50 man`, `rewarded OK`; 30 AdMob unit ID; constants và XML tag đều có trong JSON.
- Các test cấu hình kiểm cả serializer của phiên bản SDK đã cài, không chỉ parse JSON thuần.
- Chưa xác nhận hiển thị/fill quảng cáo, thao tác đóng sớm video và layout trên thiết bị thực tế.

## Remote Config / phát hành

- Giữ `global.use_test_ads: true` khi phát triển. Chỉ chuyển `false` khi kiểm thử xong để phát hành.
- `global.ads_enabled` đã bật trong assets. Các ID thật vẫn được lưu trong waterfall; SDK dùng test ads khi cờ test bật.
- Project hiện dùng param **`ads_config_v100`** trong `AdsBootstrap.REMOTE_CONFIG_KEY_ADS`, không phải tên mặc định `ads_config_v1` của kit. Publish nguyên JSON mới vào đúng param này và tăng `config_version` ở lần sửa kế tiếp; config Firebase cũ có thể ghi đè assets khi mở app.
- `google-services.json` đã có. Không có ID AdMob nào còn thiếu. Chưa publish Remote Config qua Firebase Console.
