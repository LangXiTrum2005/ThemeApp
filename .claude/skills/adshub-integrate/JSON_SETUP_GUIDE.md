# ADSHUB SDK: TÀI LIỆU ĐẶC TẢ CẤU HÌNH JSON (JSON Specification Guide)

Tài liệu này là Bảng Từ Điển (Dictionary) đặc tả chi tiết và ĐẦY ĐỦ 100% tất cả các trường (fields) cùng các giá trị (Enum) hợp lệ trong file `ads_config.json`. Mọi thiết lập của SDK sẽ tuân thủ nghiêm ngặt theo các Enum được định nghĩa tại đây.

---

## 1. THÔNG TIN ROOT (Siêu dữ liệu)
Những trường cơ bản nằm ngoài cùng của file JSON để định danh và phân loại file cấu hình.

| Trường | Kiểu | Mặc định | Ý nghĩa & Cách thiết lập |
| :--- | :--- | :--- | :--- |
| `schema_version` | `Int` | `1` | **Phiên bản Cấu trúc JSON**. Dev tăng số này khi sửa tên key/cấu trúc để App cũ không bị lỗi khi tải JSON mới. |
| `config_version` | `Int` | `1` | **Phiên bản Dữ liệu**. PM tăng số này lên khi thay đổi thông số để App tự động tải bản mới nhất về máy. |
| `environment` | `String` | `"production"` | Môi trường (`"development"`, `"staging"`, `"production"`). Dùng để phân loại rác dữ liệu trên Firebase Analytics. |

---

## 2. GLOBAL (Cài đặt Toàn cục)
Khu vực thiết lập bộ quy tắc gốc cho toàn bộ App (`global`).

| Trường | Kiểu | Mặc định | Ý nghĩa & Cách thiết lập |
| :--- | :--- | :--- | :--- |
| `admob_app_id` | `String` | `"ca-app-..."` | **App ID của AdMob**. Sẽ được SDK tự động bóc tách và nhét vào thẻ `<meta-data>` trong `AndroidManifest` lúc build. |
| `ads_enabled` | `Boolean` | `true` | **Cầu dao tổng**. Đặt `false` để tắt toàn bộ Ads trên toàn thế giới ngay lập tức. |
| `use_test_ads` | `Boolean` | `false` | **Chế độ Test**. Nếu bằng `true`, Dev không cần đổi code, SDK sẽ *tự động chặn* ID thật và thay bằng ID Test của Google (Ví dụ: `ca-app-pub-3940256099942544/...`) ở tầng Network Request để bảo vệ tài khoản. |
| `global_fullscreen_cooldown_sec`| `Int` | `60` | **Khoảng cách chống Spam (giây)** giữa 2 lần hiện Ads toàn màn hình. <br>• *Cách Tắt:* Đặt bằng `0`. |
| `max_fullscreen_per_day` | `Int` | `20` | **Giới hạn Capping**. Tối đa số lần hiện Fullscreen/ngày cho 1 user. <br>• *Cách Tắt:* Đặt bằng `-1`. |
| `skip_for_purchased_user` | `Boolean` | `true` | Tự động giấu Ads đối với User đã Purchased (đã nạp IAP). |
| `skip_for_apk_install` | `Boolean` | `false` | Bỏ qua Ads nếu ứng dụng được cài đặt từ file APK ngoài (sideload) thay vì tải từ cửa hàng chính thức. |
| `valid_installer_packages`| `[String]` | `["com.android.vending"]` | Danh sách các nguồn cài đặt hợp lệ (Vd: `com.android.vending` là Google Play). Chỉ những nguồn này mới được xem Ads nếu `skip_for_apk_install` = `true`. |
| `ump_timeout_ms` | `Long` | `15000` | Số mili-giây kiên nhẫn đợi hộp thoại xin quyền UMP load từ mạng (15000 = 15 giây). Nếu quá giờ, tự động bỏ qua để User vào App. |

---

## 2.5. VARIABLES (Biến Động - Custom User Properties)
Nơi định nghĩa các biến trạng thái tùy chỉnh của App để phục vụ cho hệ thống Rule Engine (Định Tuyến Động). Dev cấu hình các biến này để PM có thể dùng trên Web Admin ghép thành các điều kiện (Conditions).

| Trường | Kiểu | Mặc định | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- | :--- |
| `type` | `String` | `"boolean"` | Kiểu dữ liệu của biến. Các giá trị hợp lệ: `"boolean"`, `"number"`, `"string"`. |
| `default` | `Tùy type` | `null` | Giá trị mặc định nếu App chưa gửi lên. |
| `description`| `String` | `""` | Mô tả ý nghĩa của biến (Ví dụ: "Đã hoàn thành FTUE"). |

*Lưu ý:* Phía App phải gọi `AdsController.setUserProperty("tên_biến", giá_trị)` thì SDK mới lấy được data thật để so sánh.

---

## 2. TRIGGERS (Cò súng / Sự kiện)
Định nghĩa KHI NÀO một quảng cáo bị kích hoạt (`triggers`).

| Trường | Kiểu | Mặc định | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- | :--- |
| `enabled` | `Boolean` | `true` | Bật/tắt riêng lẻ Trigger này. Nếu `false` đối với Rewarded Ads, sẽ tự động cấp phát phần thưởng (Free Event). |
| `screen` | `String` | `"app"` | Tên màn hình nơi Trigger này tồn tại (Để tracking). |
| `ad_type` | `Enum (String)`| Bắt buộc | Định dạng quảng cáo của Trigger này. **Các giá trị hợp lệ:**<br>• `"app_open"`<br>• `"interstitial"`<br>• `"rewarded"`<br>• `"native"`<br>• `"native_full_screen"` (Native hiện dạng Dialog Overlay)<br>• `"banner"` |
| `rules` | `Array` | `null` | **Định Tuyến Động (Dynamic Routing)**. Mảng các quy tắc kiểm tra điều kiện để quyết định gọi `inventory_id` nào. Mỗi Rule chứa mảng `conditions` (AND logic) ghép từ các `variables`. |
| `inventory_id`| `String` | Bắt buộc | ID của Inventory Mặc định (Sẽ được gọi nếu danh sách `rules` trống hoặc không có rule nào thỏa mãn). |
| `preload_after_show` | `Array` | `[]` | **Chỉ dùng cho Fullscreen Trigger.** Khi quảng cáo toàn màn hình thật sự hiện (`onAdShowedFullScreenContent`), SDK sẽ đợi `delay_ms` rồi preload các `inventory_id` trong danh sách. Dùng để tận dụng thời gian user đang xem Inter/AppOpen/Rewarded để tải sẵn Native/Banner cho màn kế tiếp, nhưng tránh request nếu fullscreen không show. Ví dụ: `[{"inventory_id":"inv_native_lfo1","delay_ms":1000}]`. |
| `trigger.type`| `Enum (String)`| Tùy chọn | Điều kiện phụ để kích hoạt. **Các giá trị hợp lệ:**<br>• `"app_resume_after_background_sec"`: Mở app sau X giây background.<br>• `"user_click"`: Nhấn nút ấn định.<br>• `"after_action_count"`: Sau khi thực hiện action N lần.<br>• `"screen_enter"`: Khi bước vào màn hình.<br>• `"screen_enter_from"`: Khi vào màn hình Y từ màn hình X.<br>• `"unlock_feature"`: Kích hoạt khi unlock tính năng. |
| `trigger.background_sec_gte`| `Int` | `null` | Kèm với `app_resume_after_background_sec`: Số giây tối thiểu ở Background. |
| `trigger.action` | `String` | `null` | Kèm với `after_action_count`: Tên hành động (Ví dụ: `"download"`). |
| `trigger.count` | `Int` | `null` | Kèm với `after_action_count`: Số lần thực hiện hành động. |
| `trigger.is_first_session` | `Boolean` | `null` | Kèm với `screen_enter`: Có phải lần đầu mở app không. |
| `trigger.from_screen` | `String` | `null` | Kèm với `screen_enter_from`: Tên màn hình trước đó. |
| `trigger.max_wait_ms` | `Long` | `null` | Thời gian đợi mạng tối đa trước khi bỏ cuộc (Timeout). **Đặc biệt quan trọng với màn Splash/App_Open**: Đặt `10000` (10s) để buộc App đứng chờ Ads load xong mới cho qua màn hình tiếp theo, tránh bị trượt luồng (skip) vì Load mạng chậm! |
| `trigger.show_loading_ui` | `Boolean` | `null` | Có hiện màn hình "Đang tải quảng cáo..." không. |
| `reward_bypass_on_failure` | `Boolean` | `false` | **Chỉ có tác dụng với Rewarded.** Khi không lấy được ad (load fail / timeout / bị gate chặn), có phát thưởng "chữa cháy" không. `true` → SDK gọi `onRewardEarned` với đúng mảng `rewards` của inventory rồi mới `onFinished`. `false` (mặc định) → chỉ `onFinished`, user bấm nút mà không nhận được gì. Rewarded chạy `load_on_demand` (không preload) hay fail ngay lúc click hơn, nên cân nhắc bật nếu phần thưởng không phải hàng giá trị cao. |

---

## 3. SLOTS (Khung UI / Vị trí hiển thị)
Định nghĩa hành vi của các khung quảng cáo Nội tuyến (Native/Banner) gắn cứng trên giao diện (`slots`).

| Trường | Kiểu | Mặc định | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- | :--- |
| `enabled` | `Boolean` | `true` | Bật/tắt riêng lẻ khung UI này. Tắt thì View co lại 0dp. |
| `rules` | `Array` | `null` | **Định Tuyến Động (Dynamic Routing)**. Tương tự như Trigger, dùng để tráo đổi `inventory_id` dựa trên điều kiện (Ví dụ: Lần đầu xem Native giá cao, lần sau Native giá rẻ). |
| `inventory_id`| `String` | Bắt buộc | ID của Inventory Mặc định để kéo quảng cáo đắp vào khung này. |
| `refresh_policy`| `Enum (String)`| `null` | Cơ chế xử lý lại Slot/Trigger. <br>• `"on_resume"`: Force reload ngay khi Activity Resume, hiện Skeleton Loading (Giật UI). Chỉ nên dùng khi bắt buộc.<br>• `"on_click"`: (Khuyên dùng) Reload siêu mượt (Seamless). Tự xóa cache và nạp đạn mới ngầm ở Background NGAY KHI User click vào Ads. Khi quay lại App, giữ nguyên Ads cũ cho tới khi đạn mới tải xong mới tráo đổi. Kết hợp hoàn hảo với `sticky`. |

---

## 4. PLACEMENTS / INVENTORIES (Kho nạp Ads)
Định nghĩa CÁCH THỨC một quảng cáo được tải từ mạng và nằm trong RAM (`inventories`).

| Trường | Kiểu | Mặc định | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- | :--- |
| `ad_type` | `Enum (String)`| Bắt buộc | Phải đồng bộ với `ad_type` ở phần Trigger bên trên (gồm cả `"native_full_screen"`). |
| `timeout_ms` | `Long` | `5000` | Cấu hình thời gian giới hạn (mili-giây) mà UI App sẽ đứng chờ cái Inventory này load (dùng kết hợp với hàm waitForInventory). |
| `waterfall_id`| `String` | Bắt buộc | Tên của cái Waterfall sẽ được dùng để gọi quảng cáo này. |
| `priority` | `Int` | `0` | Độ ưu tiên. Số càng lớn thì càng được tải trước khi có nhiều Ads gọi cùng lúc. |
| `load_conditions` | `Array` | `null` | **Điều Kiện Tải (Load Conditions).** Thay thế cho `session_target` cũ. Là mảng các điều kiện (AND logic) sử dụng `variables`. Inventory này chỉ được phép Tải Ngầm (Preload) và Hiển Thị (Show) nếu App thỏa mãn TẤT CẢ điều kiện khai báo ở đây. |
| `native_style_id`| `String` | `null` | **Dùng cho ad_type = native**. Tham chiếu đến một style ID định nghĩa trong bảng `native_styles` để tuỳ chỉnh giao diện (Template, Màu sắc, Padding...). |
| `banner_format`| `Enum (String)`| `null` | **Dùng cho ad_type = banner**.<br>**Các giá trị (Collapsible):**<br>• `"collapsible_bottom"`: Banner ghim đáy có thể thu gọn.<br>• `"collapsible_top"`: Banner ghim đỉnh có thể thu gọn. |
| `banner_size` | `Enum (String)`| `null` | **Chỉ dùng cho ad_type = banner**.<br>**Các giá trị hợp lệ:**<br>• `"adaptive"`: Co giãn thông minh tự chèn đầy mép viền.<br>• `"medium_rectangle"`: Hình vuông 300x250dp.<br>• `"standard"`: Kích thước cố định 320x50dp. |
| `show_rule` | `String` | `null` | ID của cái luật Capping để kiểm soát tần suất hiển thị. |
| `rewards[].type` | `String` | `"reward"` | (Chỉ dùng cho Rewarded Ads). Loại phần thưởng (VD: `"unlock_feature"`, `"coin"`). Mảng có thể chứa nhiều phần thưởng. |
| `rewards[].amount` | `Int` | `1` | (Chỉ dùng cho Rewarded Ads). Số lượng phần thưởng. |
| `rewards[].grant_on`| `Enum (String)`| `"reward_earned"`| Thời điểm phát thưởng: `"reward_earned"` (Sau khi xem xong video) hoặc `"ad_closed"` (Sau khi đóng Ads). |
| `tracking_enabled`| `Boolean` | `true` | Có gửi Log Impression lên Firebase Analytics hay không. |

### 3.1. CHIẾN LƯỢC TẢI NGẦM (LOAD STRATEGY)
Đây là "Vũ khí" quan trọng nhất để điều hướng luồng Cache của SDK.

| Trường | Kiểu | Mặc định | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- | :--- |
| `mode` | `Enum (String)`| `"render_immediately"`| **Chiến thuật nạp đạn.**<br>**Các giá trị hợp lệ:**<br>• `"disabled"`: Tắt tải ngầm hoàn toàn.<br>• `"load_on_demand"`: Chỉ tải khi có lệnh (Không tự động).<br>• `"preload"`: Tự động tải ngầm trước vào RAM.<br>• `"render_immediately"`: Tới lúc kích hoạt mới lôi mạng ra tải. |
| `preload_triggers` | `Array` | `[]` | **Chỉ dùng khi mode = preload (Bắt buộc phải có nếu muốn Preload)**. Lệnh phát động việc nạp đạn. Phải khai báo mảng các object. Ví dụ: `[{"type": "app_start"}]`. Mặc định nếu mảng rỗng, SDK sẽ KHÔNG nạp ngầm!<br>**Các giá trị hợp lệ của type:**<br>• `"app_start"`: Tải ngầm ngay từ lúc App bật lên ở Splash.<br>• `"after_consent_ready"`: Ngay khi UMP Consent Form xong (Splash).<br>• `"screen_enter"`: Tải ngầm ngay khi bước vào màn hình chỉ định ở trường `screen`.<br>• `"after_ads_closed"`: User vừa tắt cái Ads này xong, lập tức tải sẵn cái tiếp theo.<br>**Khuyến nghị Native Inline:** preload ở màn ngay trước màn chứa Slot. Preload quá sớm làm matched request tăng nhưng user có thể không đi tới Slot, khiến Show Rate giảm. |
| `preload_triggers: []` + Trigger `preload_after_show` | Pattern | N/A | Pattern dành cho Native ở màn ngay sau Splash Inter: để `preload_triggers` rỗng nhằm không request từ `app_start`, rồi cấu hình `preload_after_show` ở trigger Inter Splash. Nếu Inter không show, host app nên fallback bằng `AdsController.preload()` + `waitForInventory()` trước khi chuyển màn. |
| `refresh_sec` | `Int` | `null` | **Chỉ dùng cho Banner**. Sau bao nhiêu giây thì xoay Ads mới. Đặt `0` để tắt tự làm mới. Thường dùng `30` hoặc `60`. |
| `cache_ttl_sec` | `Int` | `3600` (1 giờ) | Tuổi thọ của Ads Preload trong RAM. Quá hạn tự hủy để tránh Google đánh gậy "Ads quá đát" (Với App Open thường để 14400 - 4h). |
| `sticky` | `Boolean` | `false` | **Tính năng Sticky PeekAd**. Nếu = `true`, Ads khi lôi ra xài sẽ KHÔNG BỊ XÓA khỏi RAM (Dùng để tái sử dụng lại ở những lần Trigger kế tiếp, tránh spam Network Request). |
| `reload_after_show`| `Boolean`| `false` | (Dành cho Fullscreen). Bắn xong (Show) có tự động nạp đạn lại (Reload) cho phát sau không. |
| `reload_delay_sec` | `Int` | `0` | Chờ bao nhiêu giây trước khi nạp lại nếu `reload_after_show` = true. Giúp trì hoãn lệnh Load để Ads nằm trên cache tươi hơn cho đến tận lúc hết Interval. |
| `reload_after_render`| `Boolean`| `false` | (Dành cho Inline Native/Banner). Render xong có tự động nạp tiếp vào Cache hay không. |

> **MẶC ĐỊNH CHO REWARDED — KHÔNG PRELOAD.** Rewarded luôn do user chủ động bấm "xem quảng cáo nhận thưởng", nên nạp sẵn chỉ đốt request cho những user không bao giờ bấm. Cấu hình chuẩn: `{ "mode": "load_on_demand", "preload_triggers": [], "cache_ttl_sec": 3600, "reload_after_show": false }`.
> Khi trigger nổ mà cache rỗng, SDK tự load rồi show ngay (load đến đâu show đến đó): hiện `AdLoadingDialog` — tắt bằng `trigger.show_loading_ui: false` — rồi chờ tối đa `timeout_ms` **của inventory**; hết giờ vẫn chưa có ad thì chỉ chạy `onFinished` nên user không bao giờ kẹt, nhưng **KHÔNG được thưởng** trừ khi trigger bật `reward_bypass_on_failure: true`.
> Vì thế `timeout_ms` của inventory rewarded phải **≥ `total_timeout_ms` của waterfall** (template để `16000` khớp waterfall `16000`). Để `10000` như inventory preload là SAI: SDK bỏ cuộc trước khi thác đổ chạy xong, user bấm nút mà gần như không bao giờ thấy ads.
> `reload_after_show: true` chỉ dành cho fullscreen bắn liên tục (inter home, resume ad) — bật cho rewarded là nạp sẵn lại quả không ai chắc sẽ dùng.


---

## 4. WATERFALLS (Thác đổ / Lấy Ads từ mạng nào?)
Cấu hình danh sách nhà mạng tham gia thầu đạn (`waterfalls`).

| Trường | Kiểu | Mặc định | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- | :--- |
| `strategy` | `Enum (String)`| `"sequential"` | Chiến lược đấu thầu. Hợp lệ: `"sequential"` (Tuần tự), `"parallel"`, `"bidding"`. |
| `total_timeout_ms`| `Long` | `8000` | Tổng thời gian (mili-giây) chờ tối đa cho toàn bộ các tầng mạng trong cái Waterfall này. Quá giờ là bỏ cuộc. |
| `items` | `Array` | Bắt buộc | Mảng chứa danh sách các Tầng ưu tiên từ trên xuống dưới (Thay thế cho chữ `tiers` cũ). |
| `ad_network` | `Enum (String)`| Bắt buộc | Tên mạng lưới trúng thầu. Hợp lệ: `"admob"`, `"applovin"`, `"pangle"`, `"meta"`... |
| `ad_unit_id` | `String` | Bắt buộc | Mã ID của Quảng cáo cấp từ mạng tương ứng. |
| `timeout_ms` | `Long` | `5000` | Số mili-giây (VD: `5000` = 5 giây) SDK kiên nhẫn đợi mạng này trả Ads. Nếu mạng trả lỗi/No Fill, SDK có thể thử Tier dưới. Nếu Tier bị timeout, SDK dừng toàn bộ Waterfall vì kết nối có khả năng đang chậm hoặc mất mạng. |

---

---

## 5. NATIVE STYLES (Định dạng Giao diện Native Động)
Nơi định nghĩa các Theme (Bao gồm Template XML, Padding, Spacing, Màu sắc) để gắn vào các khung quảng cáo Native (`native_styles`). Toàn bộ giá trị màu có thể dùng HEX (Vd: `"#FFFFFF"`) hoặc khai báo Resource (Vd: `"R.color.text_primary"`) để tự động hỗ trợ Dark/Light Mode. Nếu không khai báo, hệ thống sẽ giữ nguyên Layout gốc.

| Trường | Kiểu | Mặc định | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- | :--- |
| `native_template`| `Enum (String)`| `kickads_native_full_1` | **Bắt buộc**. Chọn Template cấu trúc xương:<br>• `"kickads_native_full_1"`, `"kickads_native_full_2"`: Kích thước toàn màn hình.<br>• `"kickads_native_resume_1"`, `"kickads_native_resume_2"`: Kích thước màn hình chờ mở app (App Open Resume).<br>• `"kickads_native_inline_1"` -> `"kickads_native_inline_4"`: Dạng banner chèn ngang nội dung. |
| `is_custom_layout`| `Boolean` | `false` | Bật **Custom Layout**. Khi = `true`, bỏ qua mọi thông số màu sắc/padding/hiển thị bên dưới và giữ nguyên 100% thiết kế gốc của XML. |
| `ad_countdown_sec`| `Int` | `null` | Đếm ngược (giây) trước khi cho phép bấm nút Skip/Continue (Chỉ áp dụng cho các Template Full / Resume). |
| `bg_color` / `border_color` | `String` | `null` | Màu nền và Màu viền của khung quảng cáo (`"#HEX"` hoặc `"R.color.xxx"`). |
| `title_color` / `desc_color` | `String` | `null` | Màu Tiêu đề và Màu Mô tả. |
| `cta_bg_color` / `cta_text_color` | `String` | `null` | Màu nền và Màu chữ của Nút Action (CTA). |
| `cta_height` | `Float` | `null` | Chiều cao cố định của nút CTA (Đơn vị: dp). |
| `corner_radius` / `border_size` | `Float` | `null` | Độ bo góc và độ dày viền của toàn bộ khối quảng cáo. |
| `cta_corner_radius`| `Float` | `null` | Độ bo góc riêng biệt cho nút CTA. |
| `padding_left` / `top` / `right` / `bottom`| `Float` | `null` | Khoảng đệm (padding) 4 hướng của khung nội dung bên trong. |
| `spacing_vertical` / `horizontal` | `Float` | `null` | Khoảng cách đệm giữa các thành phần (Spacing). Dùng để kéo giãn/thu gọn khoảng trống giữa Icon, Title, Ảnh... |
| `show_title` / `show_desc` / `show_icon` / `show_media` / `show_cta` / `show_rating` | `Boolean` | `true` | Bật/Tắt (ẩn/hiện) từng thành phần trong giao diện Native. Khi tắt, Layout sẽ tự động ép mỏng lại để bù lấp khoảng trống. |

---

## 6. EXPERIMENTS (Thử nghiệm A/B)
Quản lý các chiến dịch A/B Testing trực tiếp từ Firebase mà không cần thay code (`experiments`).

| Trường | Kiểu | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- |
| `experiment_type` | `Enum (String)`| Loại Test. VD: `"inventory"` (Test vị trí hiển thị). |
| `target_type` | `Enum (String)`| Test trên cấp độ nào. Hợp lệ: `"trigger"`, `"slot"`. |
| `selection_mode` | `String` | `"ab_test"` (Chia ngẫu nhiên). |
| `candidates.variant`| `String` | Biến thể (VD: `"A"`, `"B"`, `"C"`). |
| `candidates.traffic_percent`| `Int` | Tỷ lệ user rơi vào biến thể này (VD: `50` = 50%). |
| `guardrail_metrics`| `Array` | Chỉ số theo dõi bảo vệ (VD: `["d1_retention", "crash_free"]`). |

---

## 7. SHOW RULES (Quy tắc Hiển thị / Pacing)
Tách rời logic đếm tần suất để tái sử dụng cho nhiều Trigger (`show_rules`).

| Trường | Kiểu | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- |
| `interval_sec` | `Int` | Khoảng cách tối thiểu giữa 2 lần hiện Ads cùng nhóm này. |
| `max_per_session` | `Int` | Số Ads tối đa trong 1 lần mở app. |
| `max_per_day` | `Int` | Số Ads tối đa 1 ngày. |
| `first_show_delay_sec`| `Int` | Trì hoãn X giây sau khi mở app mới được hiện Ads này. |
| `action_count_required`| `Int` | **Kiểu modulo (cách N hiện 1)**. Chỉ hiện ở các action chia hết cho X (VD: X=2 → hiện ở action 2, 4, 6...). |
| `skip_first_actions`| `Int` | **Kiểu threshold (bỏ N đầu rồi hiện hết)**. Chặn X action đầu tiên, từ action thứ (X+1) trở đi hiện **tất cả** (VD: X=1 → action 1 không hiện, action 2, 3, 4... đều hiện). Tự động giữ counter qua lần đóng Ads. Counter vẫn reset khi sang session mới (resume thật). |
| `show_only_after_background_sec`| `Int`| User phải ra ngoài Home quá X giây thì mở lại app mới bắn Ads. |

---

## 8. SCREEN RULES (Quy tắc Màn hình)
Kiểm soát sự ngột ngạt của Ads trên từng màn hình (`screen_rules`).

| Trường | Kiểu | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- |
| `max_inline_ads` | `Int` | Tổng số Ads nội tuyến (Native+Banner) tối đa được phép có trên màn hình này. |
| `conflict_groups` | `Array` | Các nhóm cạnh tranh. VD: Chặn không cho Native Top và Banner Top cùng hiện một lúc để tránh rác UI. |

---

## 9. SEGMENT RULES (Lọc User theo Hành vi & Phần cứng)
Dùng AI quét phần cứng và hành vi để dán nhãn User, từ đó thay đổi ép Capping / Pacing theo từng Profile (Ví dụ: Máy cùi thì không hiện quảng cáo nặng, User chơi nhiều thì hiện nhiều Ads). (`segment_rules`).

| Trường `conditions` | Kiểu | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- |
| `session_count_lte` | `Int` | Nếu số lần mở App <= X (Ví dụ: `10` -> User mới). |
| `session_count_gte` | `Int` | Nếu số lần mở App >= X (Ví dụ: `50` -> User cày cuốc). |
| `is_payer` | `Boolean` | User đã nạp tiền hay chưa. |
| `countries` | `[String]` | Mã ISO Quốc Gia (Vd: `["US", "GB", "CA"]`). SDK tự quét qua `Locale` hoặc SIM. |
| `manufacturers` | `[String]` | Mã Hãng Sản Xuất (Vd: `["samsung", "xiaomi", "oppo"]`). |
| `is_tablet` | `Boolean` | Quét xem máy có phải máy tính bảng (Màn hình > 600dp) hay không. |
| `min_ram_mb` | `Int` | Chỉ chạy Rule này trên máy có RAM >= X MB (Ví dụ: Máy xịn `4000` MB). |
| `max_ram_mb` | `Int` | Chỉ chạy Rule này trên máy có RAM <= X MB (Ví dụ: Máy cùi `< 2000` MB). |
| `os_version_lte`| `Int` | Chạy Rule trên các đời Android cũ (Ví dụ API <= 28 là Android 9). |

*Lưu ý: Nếu khớp `conditions`, hệ thống sẽ trả về `ads_profile` (VD: `"soft"`, `"aggressive"`, `"potato_device"`). SDK sẽ dùng tên profile này đi tìm trong bảng `show_rules` để ghi đè (override) luật Capping ban đầu.*

---

## 10. EXPERIMENTS (A/B Testing)
Bộ máy quay xổ số để tráo đổi ngầm cấu hình quảng cáo nhằm đo đạc doanh thu (`experiments`).

| Trường | Kiểu | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- |
| `enabled` | `Boolean` | Tắt/Mở chiến dịch A/B Test này. |
| `target_type` | `Enum` | Nhắm mục tiêu vào đối tượng nào để tráo đổi: `"trigger"` (Đổi cò súng) hoặc `"inventory"` (Đổi chỗ đặt). |
| `target_id` | `String` | Tên của cái ID sẽ bị tráo đổi (Ví dụ: `"home_inter_trigger"`). |
| `unit` | `Enum` | Đơn vị dính (Stickiness): `"user"` (Trọn đời 1 user 1 nhánh), `"session"` (Reset mỗi lần bật app), `"request"` (Reset mỗi lần gọi). Khuyên dùng `"user"`. |
| `candidates` | `Array` | Danh sách các nhánh tráo đổi (Mỗi nhánh gồm `variant`: Tên nhánh, `traffic_percent`: Tỉ lệ rơi vào %, `override_id`: ID Thay thế nếu bốc trúng nhánh này). Tổng `traffic_percent` phải = 100. |
| `primary_metric`| `String` | Chỉ mang tính ghi chú báo cáo (VD: `"total_revenue_per_user"`). |
| `guardrail_metrics`| `[String]` | Chỉ số an toàn cần giữ (VD: `["d1_retention", "crash_free_users"]`). |

---

## 11. COUNTRY OVERRIDES (Ghi đè Quốc gia)
Chỉnh lại config thầu (Waterfall) cho từng nước riêng biệt (`country_overrides`).

| Trường | Kiểu | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- |
| `KEY (VD: "US")` | `Object` | Các cấu hình bên trong này sẽ đè lên cấu hình mặc định (VD: Bơm Waterfall xịn hơn cho user Mỹ, Waterfall lởm hơn cho user VN). |

---

## 12. TRACKING (Theo dõi Phân tích)
Cấu hình bắn sự kiện (`tracking`).

| Trường | Kiểu | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- |
| `send_revenue_event` | `Boolean` | Bật/tắt việc bắn Event Doanh thu LTV (ROAS) lên Firebase. |
| `hash_ad_unit_id` | `Boolean` | Băm mã AdUnitID trước khi bắn Log để tránh đối thủ đọc trộm thông tin mạng quảng cáo. |

---

## 13. SAFETY RULES (Vành đai an toàn)
Những luật thép không thể bị vi phạm dù Trigger có lỗi (`safety_rules`).

| Trường | Kiểu | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- |
| `block_fullscreen_on_screens`| `Array` | Chặn tuyệt đối Fullscreen lọt vào các màn hình nhạy cảm như `"payment"`, `"subscription"`, `"privacy"`. |
| `prevent_back_to_back_fullscreen_ads`| `Boolean`| Chặn đứng trường hợp 2 cái Fullscreen bắn liên tiếp nhau gây ức chế tột độ. |
| `max_fullscreen_per_session_hard_limit`| `Int`| Dù Capping cấu hình sai, hệ thống cũng chốt hạ tối đa X Ads cho 1 Session. |

---

## 14. DEBUG (Chế độ gỡ lỗi)
Các công cụ chẩn đoán lỗi trong quá trình tích hợp (`debug`).

| Trường | Kiểu | Ý nghĩa & Cấu hình hợp lệ |
| :--- | :--- | :--- |
| `enable_debug_log` | `Boolean` | Bật/tắt việc in các dòng Log chi tiết ra Logcat (Dùng cho Dev). |
| `show_config_version_in_debug_screen` | `Boolean` | Hiện mã Version của File Config này trên giao diện màn hình Debug nội bộ. |
