---
name: adshub-integrate
description: Tích hợp AdsHub SDK (quảng cáo 1Tabb, com.onetabb:adshub) vào project Android từ A đến Z theo playbook chuẩn. Dùng khi người dùng muốn tích hợp/thêm/setup AdsHub, adshub, kickads, SDK ads nội bộ vào app; đưa Integration Brief hoặc Bảng Ads chứa ad unit ID + plan; hoặc hỏi cách dùng AdsHub trong project. Trigger words - AdsHub, adshub, tích hợp ads, tích hợp quảng cáo, integrate ads SDK, ads_config.json, kickads.
---

# Tích hợp AdsHub SDK từ A→Z

Bạn sẽ tích hợp AdsHub (SDK quảng cáo nội bộ 1Tabb) vào project Android hiện tại, tự động và trọn gói: gradle → manifest → `ads_config.json` → code → proguard/ResChiper → verify build.

## Kit tự chứa — mọi file nằm CÙNG THƯ MỤC với SKILL.md này

**KHÔNG cần repo AdsHub trên máy.** Toàn bộ tri thức nằm trong thư mục kit này (đường dẫn gốc = thư mục chứa SKILL.md, được cung cấp khi skill kích hoạt):

1. **`AI_INTEGRATION_PLAYBOOK.md`** — quy trình A→Z từng phase. **ĐỌC TOÀN BỘ file này và làm theo đúng thứ tự phase.**
2. `ads_config.template.json` — template config plan chuẩn 1Tabb (copy → thay placeholder `__*__`).
3. `AdsBootstrap.template.kt` — wrapper chuẩn (copy → thay `__APP_PACKAGE__`).
4. `AdsBaseHelpers.template.kt` — `BaseAdsActivity`/`BaseAdsFragment` hoàn chỉnh (copy nguyên file, hoặc merge `region AdsHub` vào base class có sẵn của project — KHÔNG tự viết lại tay).
5. `INTEGRATION_BRIEF_TEMPLATE.md` — form thông tin cho người dùng điền.
6. Tra cứu sâu khi cần: `JSON_SETUP_GUIDE.md` (từ điển đầy đủ mọi field của ads_config.json), `ADSHUB_DEV_GUIDE.md` (API chi tiết + code mẫu Compose — ⚠️ mục initBilling và buttonHeightS/M/L/Xl trong đó đã lỗi thời, playbook mới là chuẩn).

Thông tin cốt lõi phòng khi thiếu file: artifact `com.onetabb:adshub` trên GitHub Packages `https://maven.pkg.github.com/1Tabb-Packages/AdsHub` (auth `gpr.user`/`gpr.key` scope `read:packages` đặt ở `~/.gradle/gradle.properties`), SDK nạp `assets/ads_config.json`, entry point `com.adshub.monetization.AdsController`.

## Quy trình

1. **Thu thập brief.** Dạng phổ biến nhất: người dùng **paste nguyên Bảng Ads từ Google Sheet của AdOps** (dòng đầu `APP ID: ca-app-pub-…~…`, các cột `Vị trí | Ad name | Key remote | Loại Ads | ID Admob | Mô tả`) — đó là đủ App ID + toàn bộ ad unit ID + plan (cột Mô tả); luật dịch sang config nằm ở Phase 3.0 của playbook. Ngoài ra chấp nhận file `adshub-brief.md` / brief theo form / mô tả tự do. Nếu chưa có gì: xin họ Bảng Ads, hoặc gửi `INTEGRATION_BRIEF_TEMPLATE.md`. **Thiếu ID nào vẫn tiếp tục** với ID test Google + `use_test_ads: true` + ghi TODO, đừng dừng chờ.
2. **Version SDK:** lấy từ brief nếu có; không có thì dùng bản mới nhất ghi trong playbook Phase 1.2.
3. **Thực thi lần lượt Phase 0→7 trong playbook.** Không bỏ Phase 7 (verify build + checklist khớp ID giữa `AdsIds.kt` ↔ XML tag ↔ `ads_config.json`).
4. **Báo cáo cuối:** liệt kê file đã tạo/sửa, bảng map màn logic → màn thật, danh sách TODO (ID còn thiếu, `google-services.json`, tạo param Remote Config `ads_config_v1`), nhắc chuyển `use_test_ads → false` khi release.

## Quy tắc cứng (không được vi phạm)

- **Log đủ FULL tất cả các màn:** MỌI màn của app (kể cả màn KHÔNG có ads) đều phải có screenId trong `AdsScreen` và được `bindScreen`/`notifyScreenEnter` — không bỏ sót màn nào. Verify bằng cách liệt kê mọi Activity/Fragment/destination rồi đối chiếu.
- **Giải phẫu màn chuẩn:** mọi màn LUÔN có `slot_top` + `slot_bottom` neo cứng ngoài vùng scroll (không overlay đè content); màn có content cuộn thêm `slot_start`/`slot_end` trong content; màn có list thêm `slot_1st_item` (1 ad đầu danh sách) + `slot_3rd_item` (sau item thật thứ 3). Tất cả khai trong config, chưa dùng để `enabled:false` — chi tiết ở playbook Phase 4.5.
- **Phủ trigger TOÀN BỘ nút:** mọi nút bấm trong app đều đi qua `triggerAds("<màn>_click_<nút>")` với trigger có trong config (chưa có plan → `enabled:false` đếm ngầm); không nút nào gọi thẳng hành động.
- KHÔNG hardcode ad unit ID trong code Kotlin — tất cả nằm trong `assets/ads_config.json`.
- KHÔNG commit `gpr.key`/token vào bất kỳ file nào trong repo — auth đặt ở `~/.gradle/gradle.properties` cấp máy (chưa có thì hướng dẫn người dùng đặt; token do 1Tabb cấp).
- KHÔNG gọi AdMob API trực tiếp trong app — mọi thứ qua `AdsBootstrap` + slot tag + `triggerAds`.
- App multi-Activity: `useZeroFlashNavigation = false`.
- Điều hướng sau ads luôn bọc `runWhenResumed`/`withResumed`.
- Trigger ID `app_open_on_resume` là tên cứng của SDK — không đổi.
- Làm xong phải build thử; build đỏ thì sửa đến khi xanh mới báo xong.
