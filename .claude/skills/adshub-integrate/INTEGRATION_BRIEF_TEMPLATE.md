# AdsHub Integration Brief — {{TÊN_APP}}

> **Cách dùng:** paste nội dung theo 3 mục dưới đây vào chat (hoặc lưu thành file `adshub-brief.md` trong project) rồi nói với AI: *"Tích hợp AdsHub theo brief này"*. AI đọc playbook và tự triển khai từ A→Z.

## 1. Bảng Ads — paste NGUYÊN bảng từ Google Sheet của AdOps (bắt buộc)

Không cần chế biến gì: copy nguyên bảng (bao gồm dòng APP ID trên đầu) và paste vào đây. Format chuẩn của sheet:

```
APP ID: ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY

Vị trí | Ad name            | Key remote     | Loại Ads    | ID Admob                                | Mô tả
1      | inter_splash_high  | inter_splash   | Intersitial | ca-app-pub-XXXXXXXXXXXXXXXX/1111111111  | High floor logic
2      | inter_splash       | inter_splash   | Intersitial | ca-app-pub-XXXXXXXXXXXXXXXX/2222222222  | Inter khi user rời màn splash (phiên đầu)
3      | native_lfo1_high   | native_lfo     | Native      | ca-app-pub-XXXXXXXXXXXXXXXX/3333333333  | High floor logic
4      | native_lfo1        | native_lfo     | Native      | ca-app-pub-XXXXXXXXXXXXXXXX/4444444444  | Native ad ở màn lfo 1
5      | native_lfo2_high   | native_lfo     | Native      | ca-app-pub-XXXXXXXXXXXXXXXX/5555555555  | High floor logic
6      | native_lfo2        | native_lfo     | Native      | ca-app-pub-XXXXXXXXXXXXXXXX/6666666666  | Native ad ở màn lfo 2
7      | inter_home         | inter_home     | Intersitial | ca-app-pub-XXXXXXXXXXXXXXXX/7777777777  | Inter khi user click các button ở màn home: lần đầu vào home tần suất 1,3,5,7…; lần thứ 2 trở đi 2,4,6,8…
8      | native_inapp       | native_inapp   | Native      | ca-app-pub-XXXXXXXXXXXXXXXX/8888888888  | Native nhỏ ở các màn: Chọn content / Chatting / Cuộc gọi AI
```

AI tự hiểu các quy ước sau (không cần giải thích lại trong brief):

- **`X_high` + `X`** = một cặp waterfall: `_high` là ID giá sàn cao (thử trước), bản thường là fallback. Chỉ có 1 ID → waterfall 1 tầng.
- **`Key remote`** = tên nhóm waterfall. Nhóm chứa NHIỀU cặp (vd `native_lfo` chứa lfo1 + lfo2) → tách mỗi cặp thành 1 waterfall + 1 inventory riêng theo tên base.
- **`Loại Ads`**: Native / Banner / Intersitial / Reward / AOA. Mô tả ghi "Fullscreen native" → `ad_type: native_full_screen`.
- **`Mô tả`** chính là PLAN: màn nào, sự kiện nào, tần suất nào — AI dựng screen/slot/trigger/show_rule từ cột này. Mô tả càng rõ (màn, nút bấm, tần suất, phiên đầu/phiên sau) thì config càng chuẩn, đỡ phải đoán.
- Dòng "High floor logic/ads" chỉ là chú thích cho ID `_high`, không phải placement riêng.
- Vị trí slot đặt theo BỘ CHUẨN cố định: `slot_top`/`slot_bottom` (neo cứng trên/dưới, ngoài vùng scroll), `slot_start`/`slot_end` (đầu/cuối content cuộn), `slot_1st_item`/`slot_3rd_item` (đầu danh sách / sau item thứ 3). Mô tả ghi vị trí thế nào AI map vào slot chuẩn tương ứng.

## 2. Thông tin ngoài bảng

| Mục | Giá trị | Bỏ trống thì sao |
| :-- | :-- | :-- |
| AdMob App ID | | Chỉ cần điền nếu bảng Ads KHÔNG có dòng `APP ID:` trên đầu |
| Version SDK adshub | | Dùng bản mới nhất ghi trong playbook Phase 1.2 |
| Token GitHub Packages đã đặt ở `~/.gradle/gradle.properties`? | có / chưa | Chưa → AI hướng dẫn đặt (`gpr.user`/`gpr.key`, token do 1Tabb cấp) |
| AppsFlyer Dev Key | | Truyền `null` — tắt AppsFlyer |
| Facebook App ID / Client Token | | Bỏ meta-data Facebook trong manifest |
| `google-services.json` | có / chưa | Chưa có → mất FCM + Remote Config, app vẫn chạy; AI ghi TODO |
| App có IAP / bản Paid? | không | Có → nối `setPurchasedUserStatus()` vào billing/flavor |
| Dùng ResChiper? | không | Có → thêm whitelist `kickads_*` + `keep.xml` |
| `use_test_ads` lúc dev | `true` | Release nhớ chuyển `false` qua Remote Config |

## 3. Ghi chú plan bổ sung (tuỳ chọn)

Chỉ ghi những gì cột Mô tả chưa nói rõ, ví dụ:

```
- Luồng màn: Splash → LFO1 → LFO2 → ONB1 → Home (app mới) / Splash → Home (app cũ)
- Resume app: dùng native fullscreen "welcome back" / không dùng
- Màn cấm fullscreen: payment/subscription (nếu có) + onboarding trang 2 trở đi và welcome (mặc định nếu không ghi)
- Capping đặc biệt: chỉ ghi nếu muốn KHÁC mặc định (mặc định global/safety mở hết: cooldown 0, max/ngày 999 — tần suất do show_rules từng inventory quyết)
- App multi-Activity hay single-Activity, XML Views hay Compose (AI tự phát hiện được, chỉ ghi nếu đặc biệt)
```
