# AdsHub Integration Kit — hướng dẫn cho AI coding agent

> File này dành cho **mọi AI agent** (OpenAI Codex, Google Antigravity, Cursor, Gemini CLI, Claude Code…).
> Codex/Cursor và nhiều tool tự đọc `AGENTS.md` ở gốc project — copy/append nội dung file này vào đó
> (sửa `<đường-dẫn-kit>` cho đúng). Antigravity: paste nội dung này vào Customizations → Rules,
> hoặc để nguyên `AGENTS.md` ở gốc project. Claude Code: dùng `SKILL.md` cùng thư mục thay cho file này.
> Cách chắc chắn chạy với MỌI tool: prompt thẳng —
> "Đọc `<đường-dẫn-kit>/AI_INTEGRATION_PLAYBOOK.md` và tích hợp AdsHub theo brief sau: <Bảng Ads>".

## Nhiệm vụ

Khi người dùng yêu cầu tích hợp **AdsHub** (SDK quảng cáo 1Tabb) vào project Android:

1. **Đọc TOÀN BỘ `AI_INTEGRATION_PLAYBOOK.md` trong thư mục kit** và thực thi đúng thứ tự Phase 0→7: khảo sát project → gradle → manifest → `assets/ads_config.json` → code → ProGuard/ResChiper → Remote Config → verify build.
2. Brief của người dùng thường là **Bảng Ads paste từ Google Sheet** (dòng đầu `APP ID: ca-app-pub-…~…`, cột `Vị trí | Ad name | Key remote | Loại Ads | ID Admob | Mô tả`). Luật dịch bảng → config nằm ở Phase 3.0 của playbook. Thiếu ID nào vẫn tiếp tục với ID test Google + `use_test_ads: true` + ghi TODO.
3. **Dùng template có sẵn trong kit, KHÔNG tự chế:** `ads_config.template.json` (thay placeholder `__*__`), `AdsBootstrap.template.kt` (thay `__APP_PACKAGE__`), `AdsBaseHelpers.template.kt` (copy nguyên hoặc merge `region AdsHub` vào base class có sẵn).
4. Tra cứu sâu: `JSON_SETUP_GUIDE.md` (từ điển field config), `ADSHUB_DEV_GUIDE.md` (API + code mẫu Compose; mục initBilling và buttonHeightS/M/L/Xl trong đó đã lỗi thời — playbook là chuẩn).
5. Kết thúc bằng báo cáo: file đã tạo/sửa, bảng map màn logic → màn thật, TODO (ID thiếu, `google-services.json`, tạo param Remote Config `ads_config_v1`), nhắc `use_test_ads → false` khi release.

## Quy tắc cứng

- **Log đủ FULL tất cả các màn:** mọi màn của app (kể cả màn không có ads) đều có screenId trong `AdsScreen` và được `bindScreen`/`notifyScreenEnter`. Verify bằng cách liệt kê mọi Activity/Fragment/destination rồi đối chiếu.
- **Giải phẫu màn chuẩn:** mọi màn LUÔN có `slot_top` + `slot_bottom` neo cứng ngoài vùng scroll (không overlay đè content); màn có content cuộn thêm `slot_start`/`slot_end`; màn có list thêm `slot_1st_item` (1 ad đầu danh sách) + `slot_3rd_item` (sau item thật thứ 3). Tất cả khai trong config, chưa dùng để `enabled:false` — chi tiết ở playbook Phase 4.5.
- **Phủ trigger TOÀN BỘ nút:** mọi nút bấm trong app đều đi qua `triggerAds("<màn>_click_<nút>")` với trigger có trong config (chưa có plan → `enabled:false` đếm ngầm); không nút nào gọi thẳng hành động.
- **Block `screens` trong `ads_config.json` BẮT BUỘC có nội dung thật:** mục lục MỌI màn `"<screen_id>": {"triggers": [...], "slots": [...]}` (màn không ads = 2 mảng rỗng) — dựng ở Phase 3.0 bước 8, đồng bộ lại ở Phase 4.6, kiểm cơ học ở Phase 7. `screens` thiếu hoặc `{}` = chưa xong, không được báo hoàn thành.
- **Resume ad không được nổ ở luồng đầu:** trigger `app_open_on_resume` LUÔN kèm `"trigger": {"type": "not_on_screens", "not_on_screens": [đủ screen id của splash/language/mọi trang onboarding/welcome]}` — screen id đầy đủ, không prefix ngắn. KHÔNG dùng `safety_rules.block_fullscreen_on_screens` cho việc này (nó so theo tên Activity và chặn cả fullscreen có plan).
- **Rewarded KHÔNG preload — load đến đâu show đến đó:** mọi inventory `ad_type: "rewarded"` để `"load_strategy": { "mode": "load_on_demand", "preload_triggers": [], "cache_ttl_sec": 3600, "reload_after_show": false }`. Rewarded chỉ nổ khi user chủ động bấm nên preload sẵn là đốt request; cache rỗng lúc trigger thì SDK tự load rồi show ngay (có `AdLoadingDialog`). Kèm theo: `timeout_ms` của inventory rewarded phải ≥ `total_timeout_ms` của waterfall (mặc định `16000`, KHÔNG để `10000`), và load fail thì SDK chỉ chạy `onFinished` — muốn vẫn phát quà thì bật `"reward_bypass_on_failure": true` trên trigger.
- KHÔNG hardcode ad unit ID trong code — tất cả nằm trong `assets/ads_config.json`.
- KHÔNG commit token (`gpr.key`) vào file nào **git đang theo dõi** — auth đặt ở `~/.gradle/gradle.properties` cấp máy (token do 1Tabb cấp, scope `read:packages`).
- **KHÔNG BAO GIỜ xoá/sửa secret có sẵn trong file local của máy dev** (`local.properties`, `~/.gradle/gradle.properties`, `keystore.properties`, `.env`…). Thấy `gpr.user`/`gpr.key` trong `local.properties` thì chỉ ghi nhận "máy đã có auth" rồi ĐỂ NGUYÊN — các file này đã gitignore, không bao giờ lên repo; cũng không in giá trị token ra báo cáo. Token nằm trong file git đang theo dõi → báo người dùng và chờ họ quyết, không tự xoá.
- KHÔNG gọi AdMob API trực tiếp — mọi thứ qua wrapper `AdsBootstrap` + slot tag + `triggerAds`.
- App multi-Activity: `AdsController.useZeroFlashNavigation = false`.
- Điều hướng sau ads luôn bọc `runWhenResumed`/`withResumed`.
- Trigger ID `app_open_on_resume` là tên cứng của SDK — không đổi.
- Build đến khi xanh mới được báo xong; không bỏ Phase 7 (verify).

## Thông tin cốt lõi

- Artifact: `com.onetabb:adshub` trên GitHub Packages `https://maven.pkg.github.com/1Tabb-Packages/AdsHub` (version: theo brief, hoặc bản mới nhất ghi ở playbook Phase 1.2).
- SDK nạp cấu hình từ `assets/ads_config.json` — thiếu file này SDK im lặng tắt toàn bộ ads.
- Entry point: `com.adshub.monetization.AdsController`.
