# AdsHub — AI Integration Playbook (A→Z)

Tài liệu này dành cho **AI agent** thực thi việc tích hợp AdsHub SDK vào một project Android mới, từ đầu đến khi build xanh, chỉ dựa trên **Integration Brief** do người dùng cung cấp (xem `INTEGRATION_BRIEF_TEMPLATE.md`).

**Kit này TỰ CHỨA — mọi file tham chiếu đều nằm cùng thư mục với playbook, KHÔNG cần repo AdsHub trên máy.** Nguồn sự thật, theo thứ tự ưu tiên:
1. Playbook này + 3 template kèm theo (`ads_config.template.json`, `AdsBootstrap.template.kt`, `AdsBaseHelpers.template.kt`)
2. `JSON_SETUP_GUIDE.md` (trong kit) — từ điển đầy đủ mọi field của `ads_config.json`
3. `ADSHUB_DEV_GUIDE.md` (trong kit) — chi tiết API + code mẫu Compose (⚠️ mục 2 initBilling đã LỖI THỜI; mục buttonHeightSDp/MDp/LDp/XlDp đã LỖI THỜI — bản hiện tại chỉ có `buttonHeightDp` đơn)

**Kiến trúc bắt buộc tuân thủ:** app KHÔNG gọi AdMob API trực tiếp, KHÔNG hardcode ad unit ID trong code. Mọi thứ khai báo trong `assets/ads_config.json`; code chỉ có 2 động tác: gắn slot (`android:tag="ads_slot:<id>"` + `bindScreen`) và kéo cò (`trigger`). Mọi tương tác SDK đi qua wrapper `AdsBootstrap`.

---

## Phase 0 — Đọc brief + khảo sát project đích

**Input đến theo 1 trong 2 dạng:**

1. **Bảng Ads của AdOps (dạng phổ biến nhất)** — người dùng paste nguyên bảng từ Google Sheet: dòng đầu `APP ID: ca-app-pub-…~…`, các cột `Vị trí | Ad name | Key remote | Loại Ads | ID Admob | Mô tả`. Đây là đủ AdMob App ID + toàn bộ ad unit ID + plan (cột Mô tả). Luật dịch sang config xem Phase 3.
2. **Brief theo form** (`INTEGRATION_BRIEF_TEMPLATE.md`) hoặc mô tả tự do.

**Ngoài bảng còn cần (hỏi gọn 1 lần nếu thiếu, không chặn tiến độ):** AppsFlyer key, Facebook App ID/Client Token, `google-services.json`, cờ ResChiper/IAP, luồng màn (Splash → LFO → ONB → Home?) nếu Mô tả chưa thể hiện.

**Thiếu thông tin thì xử lý thế này (KHÔNG dừng lại hỏi từng câu):**
- Thiếu ad unit ID của placement nào → vẫn tạo đủ cấu trúc, điền ID test Google tương ứng, đặt `global.use_test_ads: true`, và ghi rõ danh sách TODO trong báo cáo cuối. ID test: banner `ca-app-pub-3940256099942544/6300978111`, inter `…/1033173712`, native `…/2247696110`, app open `…/9257395921`, rewarded `…/5224354917`.
- Thiếu AdMob App ID → dùng app id test `ca-app-pub-3940256099942544~3347511713` + TODO.
- Không nói gì về plan → dùng PLAN CHUẨN 1TABB (như template).
- Chỉ hỏi lại người dùng khi thiếu thứ không thể đoán: app chưa có màn Splash, hoặc plan mâu thuẫn với cấu trúc màn hình thật.

**Khảo sát project đích (bắt buộc trước khi sửa):**
- `settings.gradle.kts`: `repositoriesMode` (thường `FAIL_ON_PROJECT_REPOS` → repo phải khai ở đây), version catalog?
- `app/build.gradle.kts`: AGP/Kotlin version, `minSdk` (SDK cần ≥ 24), JVM target (SDK cần **Java 21** — AAR compile bytecode 65), flavor hiện có, `viewBinding`/`compose`, minify/shrink đang bật?
- Application class đã có chưa? Splash activity/fragment nào? Multi-Activity hay single-Activity? XML Views hay Compose?
- Firebase: có `google-services.json` + plugin `com.google.gms.google-services` chưa?
- `~/.gradle/gradle.properties` (Windows: `C:\Users\<user>\.gradle\gradle.properties`): kiểm tra đã có `gpr.user`/`gpr.key` chưa. Chưa có → hướng dẫn người dùng tạo file và đặt 2 dòng đó vào (token GitHub scope `read:packages` do 1Tabb cấp — người dùng xin từ quản lý SDK). **TUYỆT ĐỐI không copy token vào repo, không commit `gpr.key` vào `gradle.properties` của project**.

---

## Phase 1 — Gradle

### 1.1. `settings.gradle.kts` — repo (khai trong `dependencyResolutionManagement`)

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://artifact.bytedance.com/repository/pangle/") } // mediation Pangle — bắt buộc
        maven {
            url = uri("https://maven.pkg.github.com/1Tabb-Packages/AdsHub")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
            // Chống dependency confusion. PHẢI include CẢ HAI group:
            // adshub kéo transitive io.github.adlimitdetection:ad-limit-detection từ cùng registry.
            // Lọc thiếu group thứ hai → transitive rớt sang repo khác → 401/403.
            content {
                includeGroup("com.onetabb")
                includeGroup("io.github.adlimitdetection")
            }
        }
    }
}
```

Auth đặt ở `~/.gradle/gradle.properties` cấp máy (`gpr.user` + `gpr.key`, token scope `read:packages`). CI dùng env `GITHUB_ACTOR`/`GITHUB_TOKEN` — workflow build PHẢI truyền env này vào step gradle.

### 1.2. `app/build.gradle.kts`

```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true   // nếu app dùng XML Views
    }
    defaultConfig {
        // null nếu app không dùng AppsFlyer → AdsBootstrap truyền null vào init()
        buildConfigField("String", "APPSFLYER_DEV_KEY", "\"<key-tu-brief-hoac>null\"")
    }
    flavorDimensions += "tier"
    productFlavors {
        create("free") { dimension = "tier"; buildConfigField("boolean", "IS_PAID_TIER", "false") }
        create("paid") { dimension = "tier"; versionNameSuffix = "-paid"; buildConfigField("boolean", "IS_PAID_TIER", "true") }
    }
}
kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21) }
}

dependencies {
    // Version: lấy từ brief nếu người dùng chỉ định; không có thì dùng bản mới nhất
    // khi đóng gói kit: 0.0.1-rc09 (người phát hành SDK cập nhật số này mỗi lần release).
    implementation("com.onetabb:adshub:0.0.1-rc09")
    implementation(platform("com.google.firebase:firebase-bom:34.17.0"))
    implementation("com.google.firebase:firebase-config")     // bắt buộc cho Remote Config ads
    implementation("com.google.firebase:firebase-analytics")  // AdsAnalyticsProvider bắn GA4
    // .await() trên Task của Firebase trong AdsBootstrap — THIẾU là fetchAndActivate().await() không compile
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    // withStateAtLeast trong AdsBaseHelpers (đa số project đã có sẵn)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
}
```

Project dùng version catalog (`libs.versions.toml`) → khai qua catalog theo convention sẵn có của project, không hardcode chuỗi.

Lưu ý:
- **Toạ độ mới là `com.onetabb:adshub`** (từ rc08+). App cũ dùng `com.adshub:adshub` (rc06) — khi migrate thì ĐỔI dòng, không thêm dòng (2 group cùng lúc = duplicate classes).
- Nếu APPSFLYER_DEV_KEY không có, khai `buildConfigField("String", "APPSFLYER_DEV_KEY", "null")` — kiểu `String` nhận literal `null` được, hoặc bỏ field và truyền `null` thẳng trong AdsBootstrap.
- Flavor `tier` là quy ước nhà; nếu app không có bản paid thì bỏ flavor, thay `BuildConfig.IS_PAID_TIER` bằng `false` trong AdsBootstrap.
- SDK đã kéo sẵn: play-services-ads, UMP, mediation pangle + facebook (meta), AppsFlyer, facebook-android-sdk, lottie, firebase-messaging (api). App KHÔNG khai lại trừ khi muốn nâng version.
- Firebase: cần `google-services.json` + plugin `com.google.gms.google-services` để RC/FCM hoạt động; thiếu thì app vẫn chạy nhưng mất RC.

---

## Phase 2 — AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Thiếu AD_ID: mất attribution AppsFlyer + targeting AdMob — không lỗi build, chỉ tụt doanh thu -->
    <uses-permission android:name="com.google.android.gms.permission.AD_ID" tools:ignore="AdvertisingIdPolicy" />

    <!-- API 30+: thiếu queries → AppsFlyer không phân giải install-referrer, UMP mở Custom Tabs fail -->
    <queries>
        <intent><action android:name="com.appsflyer.referrer.INSTALL_PROVIDER" /></intent>
        <intent><action android:name="com.attribution.REFERRAL_PROVIDER" /></intent>
        <intent><action android:name="android.support.customtabs.action.CustomTabsService" /></intent>
        <intent>
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.BROWSABLE" />
            <data android:scheme="https" />
        </intent>
    </queries>

    <application>
        <!-- AAR khai sẵn App ID TEST → tools:replace BẮT BUỘC, thiếu là Manifest Merger fail -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="<ADMOB_APP_ID_TU_BRIEF>"
            tools:replace="android:value" />
        <meta-data android:name="com.google.android.gms.ads.flag.OPTIMIZE_INITIALIZATION" android:value="true" />
        <meta-data android:name="com.google.android.gms.ads.flag.OPTIMIZE_AD_LOADING" android:value="true" />

        <!-- CHỈ khi brief có Facebook App ID (AAR cũng khai key này → cần tools:replace) -->
        <meta-data android:name="com.facebook.sdk.ApplicationId"
            android:value="@string/facebook_app_id" tools:replace="android:value" />
        <meta-data android:name="com.facebook.sdk.ClientToken"
            android:value="@string/facebook_client_token" tools:replace="android:value" />

        <!-- Chặn AdActivity restart khi xoay màn/đổi theme -->
        <activity
            android:name="com.google.android.gms.ads.AdActivity"
            android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|screenSize|smallestScreenSize|uiMode"
            android:exported="false"
            android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
            tools:replace="android:configChanges, android:theme" />
    </application>
</manifest>
```

Kèm theo:
- Facebook ID đặt ở `res/values/id_fb.xml`: `<string name="facebook_app_id">…</string>`, `<string name="facebook_client_token">…</string>`. Không có thì BỎ cả 2 meta-data (AAR có bản mặc định).
- App phải có `res/drawable/ic_notification.xml` (AAR tham chiếu `@drawable/ic_notification` cho FCM; app tự định nghĩa sẽ ghi đè bản của AAR).
- SDK tự merge: permissions INTERNET/ACCESS_NETWORK_STATE/POST_NOTIFICATIONS/AD_ID và service `FMMessagingService`. App KHÔNG khai lại.

---

## Phase 3 — `assets/ads_config.json`

**Thiếu file này SDK tự chuyển `adsEnabled=false` và im lặng không hiện gì** — đây là lỗi tích hợp phổ biến nhất.

### 3.0. Luật dịch Bảng Ads → config (khi input là sheet AdOps)

1. Dòng `APP ID: …` → `global.admob_app_id` + meta-data manifest (Phase 2).
2. **Ghép cặp theo tên**: `X_high` + `X` → waterfall `X` 2 tầng — item `level: "high"` đứng trước, `level: "all"` sau (timeout mỗi tầng 15000, `total_timeout_ms` 16000). Chỉ có bản thường → waterfall 1 tầng `all`. Mô tả "High floor logic/ads" là chú thích của ID `_high`, KHÔNG phải placement riêng.
3. **`Key remote`** = tên nhóm waterfall. Nhóm 1 cặp → waterfall trùng tên key. Nhóm NHIỀU cặp (vd key `native_lfo` chứa `native_lfo1` + `native_lfo2`) → tách mỗi cặp thành 1 waterfall + 1 inventory riêng theo tên base.
4. **`Loại Ads`** → `ad_type`: Native→`native`, Banner→`banner`, Intersitial/Interstitial→`interstitial`, Reward→`rewarded`, AOA/Open app→`app_open`. Mô tả chứa "Fullscreen native"/"native full" → `native_full_screen` (kể cả khi cột ghi Native).
5. Mỗi waterfall → inventory `inv_<base>` (giữ base name của sheet để dễ đối chiếu). Load strategy theo vị trí: splash/resume → `preload` + `app_start`; native/inter gắn màn cụ thể → `preload` + `screen_enter` màn NGAY TRƯỚC màn chứa nó; banner → `render_immediately`.
6. **Cột `Mô tả` = plan.** Pattern thường gặp:
   - "ở màn X" → screen `x_view` + slot theo BỘ VỊ TRÍ CHUẨN (native/banner inline) hoặc trigger `x_<hành động>` (fullscreen). Chọn vị trí theo Mô tả: "banner đáy/ghim dưới" → `x_slot_bottom`; "ngay dưới appbar" → `x_slot_top` (neo cứng); "đầu nội dung" → `x_slot_start`; "cuối nội dung" → `x_slot_end`; "đầu danh sách" → `x_slot_1st_item`; "giữa danh sách" → `x_slot_3rd_item`.
   - "khi user click nút Y" → trigger `<màn>_click_<y>`. Một dòng liệt kê NHIỀU điểm click → nhiều trigger cùng trỏ 1 inventory.
   - "ở các màn: A / B / C" → nhiều slot (mỗi màn một slot) cùng trỏ 1 inventory.
   - "tần suất 1,3,5,7…" → show_rule `action_pattern: "odd"`; "2,4,6,8…" → `"even"`.
   - "Lần đầu vào màn X … / lần thứ 2 trở đi …" → 2 inventory dùng CHUNG 1 waterfall, mỗi inventory một show_rule (odd/even), trigger routing bằng `rules[]` + `user_property` (xem cặp `inv_inter_splash`/`inv_inter_splash_2nd` trong template).
   - "welcome back"/"resume"/"màn hình chờ mở lại app" → nối vào trigger cứng `app_open_on_resume` (`native_full_screen`, style `welcome_back`, `refresh_policy: "on_resume"`).
7. Sheet không nói gì về luồng splash/onboarding → dùng khung PLAN CHUẨN của template cho phần thiếu, hoặc hỏi người dùng 1 câu duy nhất về luồng màn.

### 3.1. Các bước dựng file

1. Copy `ads_config.template.json` → `app/src/main/assets/ads_config.json`.
2. Áp Bảng Ads theo luật 3.0 (hoặc thay `__ADMOB_APP_ID__`/`__ID_*__` nếu input là form). Placement template không có trong sheet → xoá trigger/slot/inventory/waterfall tương ứng (hoặc `enabled: false` nếu muốn bật sau qua RC). Placement sheet có mà template chưa có → tạo mới theo quy ước đặt tên.
3. Plan tuỳ chỉnh → map theo quy ước đặt tên:
   - screen: `<tên>_view` (vd `home_view`, `splash_view_1`)
   - slot: `<màn>_slot_<vị trí>` với vị trí lấy từ BỘ TỪ VỰNG CỐ ĐỊNH: `top` | `bottom` | `start` | `end` | `1st_item` | `3rd_item` (giải phẫu màn chuẩn ở 4.5) — gắn XML `android:tag="ads_slot:<slot_id>"`. **Khai TRONG CONFIG đủ bộ slot chuẩn cho MỌI màn** (`top`+`bottom` luôn có; `start`/`end` khi màn có content; `1st_item`/`3rd_item` khi màn có list): slot chưa dùng để `"enabled": false, "inventory_id": ""` — AdOps bật sau qua Remote Config không cần build lại.
   - trigger phủ TOÀN BỘ nút: mọi nút bấm trong app đều có trigger `<màn>_click_<nút>` trong config; nút chưa có plan ads → `"enabled": false, "inventory_id": ""` (đếm ngầm).
   - trigger: `<màn>_<hành động>` (vd `home_click_content`, `splash_leave_1`)
   - inventory: `inv_<format>_<placement>`; waterfall: `<format>_<placement>`
   - Chuỗi ánh xạ: **screen → slot/trigger → inventory → waterfall → ad_unit_id**
4. Quy tắc schema phải nhớ:
   - `ad_type` hợp lệ: `app_open` / `interstitial` / `rewarded` / `native` / `native_full_screen` / `banner` (khai ở INVENTORY; ghi thêm ở trigger chỉ là tài liệu — SDK đọc từ inventory).
   - `app_open_on_resume` là **trigger ID CỨNG** SDK tự bắn khi app quay lại foreground — không đổi tên. Resume ad kiểu nhà = `native_full_screen` + style `welcome_back` + `refresh_policy: "on_resume"`. Trigger `app_open` (cold start) thường `enabled: false` vì Splash đã có inter.
   - Điều kiện theo user property: `{ "type": "user_property", "variable_name": "...", "variable_operator": "==", "variable_value": ... }` — dùng trong `load_conditions` (inventory) hoặc `rules[].conditions` (trigger/slot routing).
   - `preload_triggers` rỗng = KHÔNG tự preload; phối hợp `preload_after_show` trên trigger fullscreen trước đó + fallback `preloadAndAwait()` phía app.
   - Block `variables`/`screens` chỉ là tài liệu cho AdOps (SDK `ignoreUnknownKeys`).
   - **Trigger "đếm ngầm"**: mọi điểm điều hướng nên khai trigger và app luôn gọi `triggerAds()`, kể cả chỗ chưa muốn hiện ads → để `enabled: false, inventory_id: ""`. ActionTracker vẫn cộng lượt; sau này AdOps bật ads chỉ bằng Remote Config, không cần build lại app.
5. Dev để `use_test_ads: true`; nhắc người dùng chuyển `false` khi release (qua RC hoặc file assets).
6. Validate JSON bằng cách parse thử (python/jq) trước khi kết thúc.

---

## Phase 4 — Code

### 4.0. Map plan → cấu trúc màn thật (BẮT BUỘC làm trước khi gắn bất cứ thứ gì)

Màn hình trong plan/config là màn **LOGIC** (`lan_view`, `lan2_view`, `onb1_view`…) — mỗi project hiện thực chúng khác nhau: có app 1 Activity Language với 2 trạng thái, app khác tách hẳn 2 Activity Language1/Language2. **Config JSON GIỮ NGUYÊN trong mọi biến thể** (vẫn `lan_view` + `lan2_view`, 2 slot, 2 inventory) — chỉ cách gắn phía code thay đổi. Vì vậy:

1. **Lập bảng map trước** (đưa vào báo cáo cuối): mỗi màn logic trong plan → đơn vị UI thật (Activity / Fragment / Compose destination / TRẠNG THÁI trong một màn) → pattern gắn.
2. **Cách nhận diện cấu trúc thật** — đọc code, không đoán: danh sách Activity trong Manifest, nav graph / FragmentTransaction, ViewPager2 adapter; màn "2 bước trong 1" lộ ra qua biến trạng thái + toggle visibility 2 container trong cùng layout, hoặc 1 adapter đổi dữ liệu sau khi user chọn.
3. **Chọn pattern theo kết quả map:**
   - Mỗi màn logic = 1 Activity/Fragment RIÊNG → cách gắn thường: mỗi màn 1 `adsScreenId` + container gắn tag `ads_slot:<slot_id>` trong XML của màn đó. (VD 2 Activity Language: Language1Activity → `lan_view` + `lan_slot_end`; Language2Activity → `lan2_view` + `lan2_slot_end`.)
   - Nhiều màn logic = 1 màn UI NHIỀU TRẠNG THÁI → trạng thái đầu gắn thường (tag + bind); mỗi trạng thái sau là 1 màn logic render tay: container KHÔNG gắn tag, khi chuyển trạng thái gọi `AdsBootstrap.notifyScreenEnter(screenId_mới)` → `renderSlotNow(screenId_mới, slotId_mới, container)` → `releaseSlot` khi huỷ view (chi tiết ở bảng 4.5).
   - Màn logic trong plan CHƯA tồn tại trong app (app chưa build màn đó) → không tự bịa UI: tạo đủ config + constants, ghi TODO rõ ràng trong báo cáo, hoặc hỏi người dùng đúng 1 câu nếu đó là màn trọng yếu của phễu (Splash/Language/Onboarding).
4. Số Activity không ảnh hưởng sheet AdOps: bảng ads vẫn ghi `native_lfo1`/`native_lfo2` như cũ, AdOps không cần biết app chia màn kiểu gì.

### 4.1. Files tạo mới (package `<app>/ads/`)

**`AdsBootstrap.kt`** — copy từ `AdsBootstrap.template.kt`, thay `__APP_PACKAGE__`. Nếu app không có flavor paid: thay `BuildConfig.IS_PAID_TIER` → `false`. Không dùng AppsFlyer: truyền `null`.

**`core/base/`** — copy/merge từ `AdsBaseHelpers.template.kt` (chi tiết ở mục 4.2 — không tự chế).

**`AdsIds.kt`** — constants khớp 100% với `ads_config.json` (đây là hợp đồng giữa code và config):

```kotlin
package __APP_PACKAGE__.ads

/** ID khớp với assets/ads_config.json — sửa file nào phải sửa file kia. */
object AdsScreen {
    // QUY TẮC CỨNG: liệt kê ĐỦ FULL TẤT CẢ các màn của app, KỂ CẢ màn không có ads.
    // Lý do: PreloadEngine.currentScreenId chỉ được set khi màn bind/log; trigger(),
    // safety_rules (block_fullscreen_on_screens), not_on_screens, resume-ad preload
    // và funnel tracking đều dựa vào nó. Màn không khai → SDK tưởng user còn ở màn
    // trước hoặc "unknown" → chặn nhầm ads / không chặn màn nhạy cảm / analytics sai.
    const val SPLASH_VIEW_1 = "splash_view_1" // phiên đầu
    const val SPLASH_VIEW_2 = "splash_view_2" // phiên sau
    const val LAN_VIEW = "lan_view"
    const val LAN2_VIEW = "lan2_view"
    const val ONB1_VIEW = "onb1_view"
    const val HOME_VIEW = "home_view"
    const val SETTINGS_VIEW = "settings_view"   // màn không có ads vẫn phải khai
    const val PAYMENT_VIEW = "payment_view"     // đặt tên chứa "payment" để khớp
                                                // safety_rules.block_fullscreen_on_screens (so khớp contains)
    // + TẤT CẢ các màn còn lại của app — không bỏ sót màn nào
}
object AdsSlot {
    // QUY TẮC CỨNG — BỘ SLOT CHUẨN: mỗi màn dùng đúng bộ tên vị trí cố định
    //   <màn>_slot_top      — neo cứng NGAY DƯỚI appbar, ngoài vùng scroll (LUÔN có ở mọi màn)
    //   <màn>_slot_bottom   — neo cứng đáy màn, ngoài vùng scroll (LUÔN có ở mọi màn)
    //   <màn>_slot_start    — vị trí ĐẦU TIÊN bên trong content (scrollable)
    //   <màn>_slot_end      — vị trí CUỐI CÙNG bên trong content (scrollable)
    //   <màn>_slot_1st_item — màn có list: 1 ad ở ĐẦU danh sách (trước item thật đầu tiên)
    //   <màn>_slot_3rd_item — màn có list: ad chèn giữa, SAU item thật thứ 3
    const val SPLASH_SLOT_BOTTOM = "splash_slot_bottom"
    const val LAN_SLOT_END = "lan_slot_end"
    const val LAN2_SLOT_END = "lan2_slot_end"
    const val ONB1_SLOT_END = "onb1_slot_end"
    const val HOME_SLOT_TOP = "home_slot_top"
    const val HOME_SLOT_START = "home_slot_start"
    const val HOME_SLOT_END = "home_slot_end"
    const val HOME_SLOT_BOTTOM = "home_slot_bottom"
    const val OTHER_SLOT_BOTTOM = "other_slot_bottom"
}
object AdsTrigger {
    // QUY TẮC CỨNG: TOÀN BỘ nút bấm trong app đều đi qua triggerAds() với trigger
    // khai trong config, đặt tên <màn>_click_<nút>. Nút chưa có plan ads → config để
    // enabled:false, inventory_id:"" (đếm ngầm — AdOps bật sau qua Remote Config,
    // không cần build lại app). KHÔNG nút nào gọi hành động trực tiếp mà bỏ qua trigger.
    const val SPLASH_LEAVE_1 = "splash_leave_1"
    const val SPLASH_LEAVE_2 = "splash_leave_2"
    const val ONB1_CLICK_NEXT = "onb1_click_next"
    const val HOME_CLICK_CONTENT = "home_click_content"
    const val HOME_CLICK_SETTINGS = "home_click_settings" // ví dụ nút chưa có ads — vẫn khai + đếm ngầm
    /** 2 trigger cấp SDK — AppOpenManager tự bắn, không gắn vào nút nào. */
    const val APP_OPEN = "app_open"
    const val APP_OPEN_ON_RESUME = "app_open_on_resume"
}
object AdsInventory { // chỉ khai inventory mà code cần đợi đích danh
    const val NATIVE_LFO = "inv_native_lfo"
}
```

**`AdsAnalyticsProvider.kt`**:

```kotlin
package __APP_PACKAGE__.ads

import android.content.Context
import android.os.Bundle
import com.adshub.monetization.engine.tracking.AnalyticsProvider
import com.google.firebase.analytics.FirebaseAnalytics

/** Cầu nối tracking của SDK → Firebase Analytics (GA4). */
class AdsAnalyticsProvider(context: Context) : AnalyticsProvider {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
    override fun logEvent(eventName: String, params: Bundle) {
        firebaseAnalytics.logEvent(eventName, params)
    }
}
```

(Tuỳ chọn nâng cao như 2 app tham chiếu: bản debug đẩy thêm event lên Firebase Realtime Database để AdOps soi realtime — chỉ thêm khi brief yêu cầu.)

**Màu native ads** — thêm vào `res/values/colors.xml`, map theo Design System app (dùng token riêng `ad_native_*`, KHÔNG dựa vào `R.color.white` — project có thể không định nghĩa):
```xml
<color name="ad_native_bg">#FFFFFF</color>
<color name="ad_native_text_primary">#1A1A1A</color>
<color name="ad_native_text_secondary">#666666</color>
<color name="ad_native_button_bg">#2196F3</color>
<color name="ad_native_button_text">#FFFFFF</color>
```

### 4.2. Base class helpers — copy từ `AdsBaseHelpers.template.kt` (XML Views)

**KHÔNG tự chế phần này** — template `AdsBaseHelpers.template.kt` đã chứa bản hoàn chỉnh `BaseAdsActivity` + `BaseAdsFragment` (thay `__APP_PACKAGE__`). Hai cách áp:

- **Project chưa có base class** → copy nguyên file vào `core/base/`, cho toàn bộ Activity/Fragment kế thừa.
- **Project đã có BaseActivity/BaseFragment riêng** → copy nguyên **`region AdsHub`** trong template vào base có sẵn (chỉ thêm member, không đổi hành vi cũ). Template dùng `findViewById(android.R.id.content)` làm root quét nên KHÔNG phụ thuộc kiểu ViewBinding của project; nếu base có sẵn dùng binding thì có thể override `adsRootView = binding.root`, không bắt buộc.

Những chi tiết template đã lo sẵn (đây là lý do không được viết lại tay):
- **Quy tắc phủ sóng màn hình:** MỌI màn khai `adsScreenId` và đều bind — kể cả màn KHÔNG có ads. `bindScreen()` trên màn không slot vẫn làm 3 việc sống còn: set `currentScreenId` (safety rules + `fromScreen` đúng), kích `preload_triggers.screen_enter` màn kế, tự `logScreenView` (hoãn qua `runOnAdClosed` — chỉ đếm khi user thật thấy màn). `adsScreenId = null` CHỈ dành cho Splash.
- Vòng đợi `ConfigManager.isInitialConfigLoaded` trước khi bind (bind sớm = slot chết im lặng).
- Activity bind ở `onPostCreate` (sau `setContentView` của lớp con); Fragment bind ở `onResume` + cờ `hasBoundAds` (ViewPager2 tạo view trước khi hiện — bind sớm là đốt fill), reset cờ ở `onDestroyView` cho back-stack.
- `triggerAds()` wrapper và `runWhenResumed()` (bọc điều hướng trong `onFinished` — xem Bẫy #1).
- `autoStartAdsColdStart` fallback khi process mở thẳng vào màn khác Splash (widget/notification).

Màn/trạng thái render tay không qua `bindScreen` → `AdsBootstrap.notifyScreenEnter()` (đã kèm log màn).

### 4.3. Application class

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this) // tường minh — AdsAnalyticsProvider cần ngay sau đó
        AdsBootstrap.onApplicationCreate(
            application = this,
            isOnboardingCompleted = /* đọc từ SharedPreferences/DataStore, false nếu app mới */,
        )
    }
}
```

### 4.4. Splash — luồng chuẩn (thứ tự là linh hồn, đừng đảo)

```kotlin
class LaunchActivity : BaseAdsActivity() { // hoặc base có sẵn của project đã merge region AdsHub
    override val adsScreenId get() = if (isFirstSession) AdsScreen.SPLASH_VIEW_1 else AdsScreen.SPLASH_VIEW_2
    override val bindAdsScreenOnCreate = false  // config chưa nạp — tự bind ở bước 3
    override val autoStartAdsColdStart = false  // Splash tự drive và ĐỢI kết quả
    override val allowAdsConsentForm = true
    private var hasNavigated = false

    private fun startSplashFlow() {
        lifecycleScope.launch {
            val minSplash = async { delay(2_000L) }              // splash tối thiểu, chạy SONG SONG

            AdsBootstrap.awaitColdStart(this@LaunchActivity)      // 1. UMP → RC → initNetworks → preload
            AdsBootstrap.awaitPreloadReady()                      // 2. đợi PreloadEngine kích hoạt
            bindAdsScreen()                                       // 3. giờ mới bind → bắn screen_enter
            minSplash.await()                                     // 4. đủ 2s tối thiểu

            val trigger = if (isFirstSession) AdsTrigger.SPLASH_LEAVE_1 else AdsTrigger.SPLASH_LEAVE_2
            withTimeoutOrNull(12_000L) { AdsBootstrap.awaitAdReadyFor(trigger) }  // 5. đợi đạn Inter

            if (needsLanguageScreen) {                            // 6. màn kế cần native → chờ sẵn
                withTimeoutOrNull(5_000L) { AdsBootstrap.preloadAndAwait(AdsInventory.NATIVE_LFO) }
            }

            triggerAds(trigger) { goToNext() }                    // 7. bắn Inter rồi mới điều hướng
        }
    }

    private fun goToNext() {
        if (isFinishing || isDestroyed || hasNavigated) return
        hasNavigated = true
        markOpened() // lật cờ hasOpenedBefore ở CUỐI luồng
        startActivity(/* Language / Onboarding / Home theo trạng thái */)
        // KHÔNG finish() ngay: SDK còn Dialog gắn vào cửa sổ này → WindowLeaked
        AdsController.runOnAdClosed("finish_splash") { finish() }
    }
}
```

Process restore (OS kill rồi mở lại) — ở màn chính:
```kotlin
if (savedInstanceState != null) {
    AdsController.notifyAppRestoredFromBackground() // không phải cold start thật — đừng chặn nhầm resume ad
}
```

### 4.5. Gắn ads theo từng loại

**GIẢI PHẪU MÀN CHUẨN (bắt buộc cho mọi màn).** Mỗi màn dựng theo khung dọc cố định — `slot_top`/`slot_bottom` neo cứng NGOÀI vùng scroll nên **không bao giờ đè lên nội dung**, `slot_start`/`slot_end` nằm TRONG content cuộn:

```xml
<LinearLayout android:orientation="vertical"
    android:layout_width="match_parent" android:layout_height="match_parent">

    <!-- AppBar (nếu có) -->

    <FrameLayout android:tag="ads_slot:home_slot_top"
        android:layout_width="match_parent" android:layout_height="wrap_content" />

    <!-- CONTENT: chiếm hết phần còn lại (weight=1), scroll bên trong -->
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1">
        <LinearLayout android:orientation="vertical"
            android:layout_width="match_parent" android:layout_height="wrap_content">

            <FrameLayout android:tag="ads_slot:home_slot_start"
                android:layout_width="match_parent" android:layout_height="wrap_content" />

            <!-- ... nội dung thật của màn (banner hero, buttons, list...) ... -->

            <FrameLayout android:tag="ads_slot:home_slot_end"
                android:layout_width="match_parent" android:layout_height="wrap_content" />
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>

    <FrameLayout android:tag="ads_slot:home_slot_bottom"
        android:layout_width="match_parent" android:layout_height="wrap_content" />
</LinearLayout>
```

Quy tắc khung:
- **Mọi màn LUÔN có `slot_top` + `slot_bottom`** trong layout, kể cả khi plan hiện tại không dùng — SDK thu về 0dp khi slot tắt, AdOps bật lại qua RC bất cứ lúc nào mà không cần build.
- KHÔNG đặt slot_top/slot_bottom dạng overlay (FrameLayout đè/floating) — luôn là hàng riêng trong LinearLayout dọc (hoặc ConstraintLayout chain) để ads không bao giờ che nội dung.
- Màn có content cuộn → thêm `slot_start` (vị trí đầu tiên trong content) và `slot_end` (vị trí cuối cùng). VD: `slot_start → banner hero → button features → slot_end`.
- Màn dùng RecyclerView làm content → RecyclerView là phần weight=1; ad trong list dùng `slot_1st_item`/`slot_3rd_item` (xem bảng dưới), vẫn giữ slot_top/slot_bottom neo ngoài.
- Compose tương đương: `Column { SlotTop; Box(Modifier.weight(1f)) { /* content scroll + SlotStart/SlotEnd */ }; SlotBottom }`.

| Loại | Cách gắn |
| :-- | :-- |
| **Banner / Native inline** | XML: `<FrameLayout android:tag="ads_slot:<slot_id>" android:layout_width="match_parent" android:layout_height="wrap_content" />` đặt đúng vị trí theo giải phẫu màn chuẩn + màn hình khai `override val adsScreenId`. Hết. SDK tự skeleton/ẩn hiện. |
| **Interstitial / Rewarded / Native full-screen** | `triggerAds(AdsTrigger.X) { runWhenResumed { điều hướng/logic } }` — `onFinished` LUÔN chạy kể cả khi ads bị chặn, nên user không bao giờ kẹt. Rewarded cần quà thật thì truyền `onRewardEarned = { rewards -> … }`. |
| **PHỦ TRIGGER TOÀN BỘ NÚT (bắt buộc)** | MỌI nút bấm trong app — kể cả nút chưa có plan ads — đều đi qua `triggerAds("<màn>_click_<nút>") { hành động }`, và trigger đó PHẢI khai trong config (chưa dùng → `enabled:false, inventory_id:""`). ActionTracker vẫn đếm ngầm; AdOps bật ads cho bất kỳ nút nào sau này chỉ bằng Remote Config. KHÔNG nút nào set onClick gọi thẳng hành động mà bỏ qua trigger. |
| **App resume ad** | Không code gì — SDK tự bắn trigger `app_open_on_resume`. |
| **Màn KHÔNG có ads** | VẪN khai `adsScreenId` + để base class bind như thường — bắt buộc để log đủ full tất cả các màn và giữ `currentScreenId` đúng (safety rules, resume ad, preload màn kế). Layout vẫn dựng theo giải phẫu chuẩn (slot_top/bottom có sẵn, config tắt). |
| **Native màn 2 trạng thái** (Language bước 2…) | Container thứ 2 KHÔNG gắn tag trong XML (bindScreen render mọi tag kể cả view đang `gone` → đốt fill). Khi chuyển trạng thái, đủ 4 bước: `releaseSlot(slotId1)` → `container1.removeAllViews()` → **gỡ HẲN container1 khỏi cây view** bằng `(container1.parent as? ViewGroup)?.removeView(container1)` → `AdsBootstrap.notifyScreenEnter(screenId2)` (kích preload + log màn) → `AdsBootstrap.renderSlotNow(screenId2, slotId2, container2)`; `releaseSlot(slotId2)` ở `onDestroyView`. Bước gỡ container là BẮT BUỘC, `gone()` KHÔNG đủ: view GONE vẫn `isAttachedToWindow == true` nên slot trạng thái 1 còn nguyên trong danh sách quét của màn và `refresh_policy` sẽ nạp native mới vào khung đã ẩn mỗi lần resume (Bẫy #14). |
| **Native trong RecyclerView / list** | Màn có list item → chuẩn hoá 2 slot: **`<màn>_slot_1st_item`** = 1 ad ở ĐẦU danh sách (adapter chèn ad item ở vị trí 0, trước item thật đầu tiên) và **`<màn>_slot_3rd_item`** = ad chèn giữa, SAU item thật thứ 3 (list hiển thị: ad → item1 → item2 → item3 → ad → item4…). Item layout dùng tag `list_ads_slot:<slot_id>` (KHÔNG phải `ads_slot:`); adapter `onBind` gọi `AdsSlotScanner.processSingleSlot(screenId, slotId, container)`, `onViewRecycled` gọi `removeSlotFromRegistry(slotId)`. Không gọi `AdsController.processInlineSlot` trực tiếp. |
| **Compose** (app Compose) | Slot: composable `AdsHubSlot` (tag `compose_ads_slot:` + `AdsSlotScanner.processSingleSlot`) và nút trigger `AdsHubClickableBox` — code mẫu trong `ADSHUB_DEV_GUIDE.md` (kit) mục 3 bước 3 và mục 4.3. **Quy tắc log đủ full các màn ở Compose:** mỗi destination gọi `AdsBootstrap.notifyScreenEnter(screenId)` trong `LaunchedEffect(Unit)` (đã kèm logScreenView + kích preload) — kể cả destination không có ads. Single-Activity Compose có thể giữ `useZeroFlashNavigation = true` (xem chú thích Zero-Flash trong `AdsBootstrap.template.kt`). |

---

## Phase 5 — ProGuard / shrinkResources / ResChiper

### 5.1. `app/proguard-rules.pro` (khi `isMinifyEnabled = true`)

AAR đã ship consumer rules (model, mediation, NativeAdView…). App bổ sung:

```proguard
# ==================== AdsHub SDK ====================
# Config parse bằng kotlinx.serialization — R8 đổi tên model là parse hỏng toàn bộ
-keep class com.adshub.monetization.model.** { *; }
-keepclassmembers class com.adshub.monetization.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.adshub.monetization.**$$serializer { *; }
-keepclassmembers class com.adshub.monetization.** { *** Companion; }
-keep class com.adshub.monetization.AdsController { *; }
-keep class com.adshub.monetization.engine.tracking.AnalyticsProvider { *; }
-dontwarn com.adshub.monetization.**

# GAID — AppsFlyer/AdMob gọi qua reflection
-keep class com.google.android.gms.ads.identifier.** { *; }
```

Khuyến nghị thêm: tắt R8 full optimization — `release { optimization { enable = false } }` nếu AGP hỗ trợ, hoặc dùng `proguard-android.txt` thay vì `-optimize`.

### 5.2. `res/raw/keep.xml` (khi `isShrinkResources = true`)

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:keep="@layout/tt_*, @anim/tt_*, @drawable/tt_*, @string/tt_*, @color/tt_*,
    @dimen/tt_*, @integer/tt_*, @style/tt_*, @attr/tt_*,
    @layout/kickads_*, @color/kickads_*, @id/ad_*, @id/btn_get_started" />
```
(Nếu dùng layout resume custom, thêm `@layout/layout_native_resume`.)

### 5.3. ResChiper (chỉ khi brief bật)

SDK tra resource bằng `getIdentifier` theo TÊN (`kickads_*`) — obfuscate tên là getIdentifier trả 0, native layout chết (chỉ dính bản AAB). Whitelist bắt buộc trong `configure<io.github.goldfish07.reschiper.plugin.Extension>`:

```kotlin
whiteList = setOf(
    "res/raw", "res/raw/*", "res/xml", "res/xml/*", "*.R.raw.*", "*.R.xml.*",
    "*.R.layout.kickads_*",
    "*.R.id.ad_*",
    "*.R.id.kickads_*",
    "*.R.id.btn_get_started",
    "*.R.drawable.kickads_*",
    "*.R.drawable.ic_kickads_*",
    "*.R.drawable.bg_kickads_*",
    "*.R.drawable.bg_native_*",
    "*.R.drawable.ic_notification",
    "*.R.drawable.ic_close",
    "*.R.color.kickads_*",
    "*.R.string.kickads_*",
    "*.R.attr.kickads_*",
    "res/*/kickads_*",
    // + whitelist Firebase/Crashlytics và tt_* (Pangle) của app nếu có
)
```
Plugin: classpath `io.github.goldfish07.reschiper:plugin:0.1.0-rc6` (root), apply `id("io.github.goldfish07.reschiper")` (app). Build AAB: `./gradlew :app:resChiper<Flavor>Release`.

---

## Phase 6 — Firebase Remote Config

1 param duy nhất: **`ads_config_v1`** (kiểu String/JSON) = nguyên chuỗi nội dung `ads_config.json`. SDK không tự đọc Firebase — `AdsBootstrap.fetchAdsConfigJson()` fetch rồi bơm `AdsController.updateRemoteConfig()`.

Hướng dẫn người dùng sau khi tích hợp: Firebase Console → Remote Config → tạo param `ads_config_v1` → paste JSON (nhớ tăng `config_version` mỗi lần sửa) → Publish. Từ đó bật/tắt ads, đổi capping, tráo waterfall **không cần build lại app**. Bẫy debug: sửa `assets/ads_config.json` trên máy là chưa đủ nếu RC đã publish — RC ghi đè mỗi lần mở app.

---

## Phase 7 — Verify (bắt buộc trước khi báo xong)

1. `./gradlew :app:assembleDebug` (Windows: `.\gradlew.bat`; có flavor thì `assemble<Flavor>Debug`) — build phải xanh, đỏ thì sửa tới khi xanh.
2. Checklist tĩnh:
   - [ ] `app/src/main/assets/ads_config.json` tồn tại, parse được, không còn placeholder `__*__`
   - [ ] Mọi `slot_id`/`trigger_id`/`screen` trong `AdsIds.kt` + XML tag khớp key trong JSON; mọi `inventory_id` trong trigger/slot tồn tại trong `inventories`; mọi `waterfall_id` tồn tại trong `waterfalls`
   - [ ] **Log đủ FULL tất cả các màn:** liệt kê mọi Activity/Fragment/destination của app rồi đối chiếu — màn nào cũng có hằng số trong `AdsScreen` và có `adsScreenId`/`bindScreen` (hoặc `notifyScreenEnter` với trạng thái render tay), KỂ CẢ màn không có ads. Không màn nào bị bỏ sót
   - [ ] **Bảng map 4.0 đầy đủ:** mỗi màn logic của plan đã chỉ đích danh Activity/Fragment/trạng thái thật + pattern gắn; màn chưa tồn tại được đánh dấu TODO, không màn logic nào bị gắn nhầm chỗ
   - [ ] **Giải phẫu màn chuẩn:** mọi màn có `slot_top` + `slot_bottom` neo cứng ngoài vùng scroll (không overlay đè content); màn có content cuộn có `slot_start`/`slot_end`; màn có list có `slot_1st_item`/`slot_3rd_item`; TẤT CẢ đều khai trong config (chưa dùng thì `enabled:false`)
   - [ ] **Phủ trigger toàn bộ nút:** liệt kê mọi nút bấm/điểm click trong app rồi đối chiếu — nút nào cũng đi qua `triggerAds` với trigger có trong config (kể cả `enabled:false` đếm ngầm), không nút nào gọi thẳng hành động
   - [ ] Manifest có APPLICATION_ID + `tools:replace`
   - [ ] `use_test_ads: true` (dev) + TODO list các ID còn thiếu
3. Chạy thử (nếu môi trường cho phép): filter Logcat `AdsHub:` — thấy `[CONFIG] Đã nạp từ Assets.` là config sống; `[TRACKING] <waterfall>_<tier>_load_success / show_success` là ads chạy. SDK tự ghi lý do chặn (`[CAPPING]`, `[SAFETY]`, `[GATE]`).
4. Báo cáo cuối cho người dùng: những gì đã tạo/sửa, TODO (ID thiếu, google-services.json, RC param), và nhắc release: `use_test_ads → false`.

---

## Bẫy đã biết (đã gặp thật khi tích hợp)

| # | Triệu chứng | Nguyên nhân → Fix |
| :-- | :-- | :-- |
| 1 | Đóng fullscreen xong đứng im không sang màn, log sạch | SDK gọi `onFinished` khi FragmentManager còn "state saved"; `commit()` ném IllegalStateException và SDK NUỐT nó → luôn bọc điều hướng bằng `runWhenResumed`/`withResumed` |
| 2 | Không có ads nào, không lỗi gì | Thiếu/hỏng `assets/ads_config.json` → SDK im lặng `adsEnabled=false`. Xem Logcat `[CONFIG]` |
| 3 | Resume ad hiện template full-screen lạ thay vì layout custom | `native_styles.<id>.native_template` sai tên → SDK im lặng fallback `kickads_native_full_2`. Kiểm tra tên layout |
| 4 | Preload treo, native trạng thái 2 không bao giờ load | Trạng thái render tay không đi qua `bindScreen` → phải gọi `AdsBootstrap.notifyScreenEnter(screenId)` trước `renderSlotNow` |
| 5 | Native AAB build hỏng layout, APK debug ngon | ResChiper obfuscate tên resource `kickads_*` → whitelist (Phase 5.3) |
| 6 | Trigger/slot sau splash bị chặn vĩnh viễn, `isFullScreenAdShowing` kẹt `true` | App multi-Activity mà `useZeroFlashNavigation = true`, Activity show ad bị finish → set `false` |
| 7 | Splash WindowLeaked | `finish()` ngay sau `startActivity` khi SDK còn Dialog → `AdsController.runOnAdClosed("finish_splash") { finish() }` |
| 8 | 401/403 khi resolve dependency | Token thiếu scope `read:packages`, hết hạn, hoặc content filter thiếu `io.github.adlimitdetection` |
| 9 | Show Rate thấp, request nhiều | Preload quá sớm nhiều bước trước màn chứa slot; slot ẩn vẫn gắn tag; xem mục 4.5 và `preload_after_show` |
| 10 | Sửa `assets/ads_config.json` không ăn | Remote Config `ads_config_v1` đã publish ghi đè mỗi lần mở app |
| 11 | Build lỗi class file version | App JVM target < 21 — AAR compile bytecode 65 → nâng toolchain Java 21 |
| 12 | `initBilling` không tồn tại | API đã bị vô hiệu trong SDK → dùng `AdsController.setPurchasedUserStatus(boolean)` |
| 13 | Resume ad không show dù config đúng | `AppOpenManager` có các cửa chặn tự nhiên: chỉ nổ ở HOT resume (cold/warm start bỏ qua), vừa đóng fullscreen khác <2s, user vừa click ad quay lại (`skip_resume_ad_after_click`), đang có fullscreen khác showing/pending, chưa đủ giây background, hoặc cache rỗng vì inventory không preload từ `app_start`. → Lọc Logcat `AppOpenManager` — SDK in rõ lý do "Bỏ qua App Open On Resume vì…" từng lần. Lưu ý field `preload_on_app_start` KHÔNG tồn tại trong schema trigger (bị nuốt im lặng) — preload do `load_strategy` của inventory quyết |
| 14 | Màn nhiều trạng thái: slot trạng thái 1 đã ẩn vẫn nạp ad mỗi lần resume; show rate tụt, log có `show_request` mà không có `show_success` | `releaseSlot()` CHỈ xoá `slotRegistry`, KHÔNG gỡ slot khỏi danh sách quét của màn; view `gone()` vẫn `isAttachedToWindow == true` nên lọt qua chốt lọc trong `AdsSlotScanner.onResume` → slot có `refresh_policy` (`on_resume`/`on_click`, kể cả bật sau bằng Remote Config) nạp native vào khung đã ẩn: đốt fill, chiếm suất ad density, không bao giờ có impression. → Khi chuyển trạng thái phải gỡ hẳn container: `(container.parent as? ViewGroup)?.removeView(container)`, không chỉ `gone()` (mục 4.5) |
