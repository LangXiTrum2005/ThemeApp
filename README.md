[![CI Pipeline](../../actions/workflows/ci.yml/badge.svg)](../../actions/workflows/ci.yml)

# TeamplateAndroid

Android project with an automated release pipeline on GitHub Actions: release branch
management, RC/production tagging from PR comments, signed APK/AAB builds, and
distribution to GitHub Releases, Firebase App Distribution, Google Drive and Discord.

## Documentation

| Document | Contents |
|---|---|
| [SECRETS_SETUP.md](SECRETS_SETUP.md) | **Start here.** Hướng dẫn từng bước tạo secrets, variables, labels và quyền Actions |
| [CI_CD_SETUP.md](CI_CD_SETUP.md) | Kiến trúc pipeline, ràng buộc khi sửa, việc còn tồn đọng |
| [GITHUB_WORKFLOWS_README.md](GITHUB_WORKFLOWS_README.md) | Quick start, command reference, troubleshooting |
| [github_workflow.md](github_workflow.md) | Branch strategy, diagrams, per-workflow detail |
| [ontabb_rate/README.md](ontabb_rate/README.md) | Module đánh giá: điểm kích hoạt, Remote Config `rating_show`, hai kiểu hiển thị |

## The short version

```
Issue titled "1.2.0"  →  release/1.2.0 branch + tracking PR
Merge features into release/1.2.0
Comment "/build rc" in the tracking PR      →  tag 1.2.0-rc01  →  build + distribute
Comment "/build release" in the tracking PR →  tag 1.2.0       →  build + distribute + auto-merge PR
```

## Build

```bash
./gradlew :app:assembleRelease
```

Release builds fall back to the debug signing key when no signing properties are
supplied. CI passes them explicitly:

```bash
./gradlew :app:assembleRelease :app:bundleRelease -PciVersionCode=42 -PciVersionName=1.2.0-rc01 -PANDROID_KEY_ALIAS=... -PSIGNING_KEY_STORE_PATH=... -PANDROID_KEYSTORE_PASSWORD=... -PANDROID_KEY_PASSWORD=...
```

`ciVersionCode` / `ciVersionName` override `versionCode` / `versionName` in
[app/build.gradle.kts](app/build.gradle.kts). Without them the build uses `1` / `"1.0"`.

## Project facts

| | |
|---|---|
| Modules | `:app` + `:ontabb_rate` (module đánh giá dùng chung) |
| applicationId / namespace | `com.onetabb.template.android` (debug thêm hậu tố `.dev`) |
| compileSdk / minSdk / targetSdk | 37 / 28 / 37 |
| Product flavors | không có, chỉ `debug` / `release`; ads test hay real do `use_test_ads` trong `ads_config.json` / Remote Config quyết định, không tách flavor |
| AGP / Gradle / JDK / Kotlin | 9.2.1 / 9.4.1 / 21 / 2.4.x (bản Kotlin đi kèm AGP) |
| Release minification | **on** — R8 + `shrinkResources`, rule ở [app/proguard-rules.pro](app/proguard-rules.pro) |
| UI | XML + ViewBinding, DI bằng Hilt, ads qua AdsHub SDK |
| Nút bấm | `AppButton` dùng chung: text + icon tuỳ chọn + icon quảng cáo, ba kiểu `primary` / `outline` / `text` |
| Chủ đề | nền sáng; đổi màu ở [colors.xml](app/src/main/res/values/colors.xml), layout không hardcode mã màu |

## Luồng màn hình có sẵn

Template đi kèm nguyên luồng first-open đã nối AdsHub, build và chạy được ngay:

```
LaunchActivity (splash_view_1/2)
   -> LanguageActivity (lan_view -> lan2_view)
   -> OnboardingActivity: SurveyFragment (onb1_view -> onb1_click_view)
                          OnboardingPageFragment x2 (onb2_view, onb3_view)
                          WelcomeFragment (welcome_view)
   -> MainActivity (main_view) + 4 tab placeholder (home/tab2/tab3/settings)
        -> SettingLanguageActivity (setting_lan_view) từ hàng "Language" trong tab Settings
```

`WelcomeBackActivity` là màn resume rời, chưa nối vào luồng — dùng khi cần màn chào lúc mở lại app.

Đổi ngôn ngữ có HAI màn tách bạch, cố ý không dùng chung: `LanguageActivity` của luồng
first-open (chặn back, có native ad, tự đi tiếp sang onboarding) và `SettingLanguageActivity`
đi từ Settings (có back và Save, không chạy ads). Hai màn dùng chung `LanguageViewModel` cùng
`LanguageAdapter`, nên sửa danh sách ngôn ngữ chỉ sửa một chỗ.

## Module đánh giá `:ontabb_rate`

Hộp thoại chấm sao dùng chung, đã nối sẵn vào `App.kt` và `MainActivity`. Cách cấu hình, các
điểm kích hoạt và tham số Remote Config `rating_show` nằm ở [ontabb_rate/README.md](ontabb_rate/README.md).

Quy ước bắt buộc khi thêm màn mới:

- Mọi màn override `adsScreenId` bằng hằng số trong
  [AdsIds.kt](app/src/main/java/com/onetabb/template/android/ads/AdsIds.kt), kể cả màn không có ads.
- Mọi nút đi qua `triggerAds("<màn>_click_<nút>")`; điều hướng đặt trong `onFinished` và bọc `runWhenResumed`.
- Nút bấm dùng [AppButton](app/src/main/java/com/onetabb/template/android/core/widget/AppButton.kt)
  thay vì `Button` trần. Nút nào bấm vào sẽ mở quảng cáo (reward, inter, native full-screen) thì
  bật `app:abShowAdIcon="true"` để user biết trước.
- Màn giải thích trước khi xin quyền dùng
  [PermissionDialog](app/src/main/java/com/onetabb/template/android/presentation/dialog/PermissionDialog.kt):
  một khung dùng chung, mỗi quyền truyền icon và chữ riêng qua `PermissionDialogArgs`. Dialog chỉ
  trả lời "user có đồng ý đi tiếp không"; việc gọi launcher xin quyền vẫn nằm ở màn gọi.
- Mỗi màn có `<màn>_slot_top` + `<màn>_slot_bottom` neo cứng ngoài vùng cuộn.
- Thêm màn/slot/trigger vào code thì phải khai đúng tên trong
  [assets/ads_config.json](app/src/main/assets/ads_config.json).

Đổi bảng màu thì đổi CẢ CỤM: token trong `colors.xml`, `windowLightStatusBar` trong
`themes.xml` và `SystemBarStyle` trong `BaseActivity`. Sửa mỗi `bg_main` sẽ ra chữ trùng màu nền.
Riêng thẻ đánh giá của `:ontabb_rate` có bộ màu tối riêng, cố ý không bám theo app.

Trước khi phát hành, thay hết phần đánh dấu `TODO(project)`: AdMob App ID trong manifest và
`global.admob_app_id`, ad unit ID thật trong `waterfalls` (hiện là ID test của Google,
`use_test_ads: true`), Facebook App ID / Client Token, key Remote Config `ads_config_v1`,
`Constants.PRIVACY_POLICY_URL`, `Constants.EMAIL_SUPPORT`, ảnh onboarding và 4 tab của MainActivity. Màn Language đã có sẵn
cờ thật cho 29 ngôn ngữ, chỉ cần cắt bớt danh sách cho khớp số `values-<mã>` mà app thật sự dịch.

## Distribution channels

Every channel is optional and self-disabling: `ci.yml` probes for the relevant
secrets and skips the job when they are absent.

- **GitHub Releases** — always. Tags containing a hyphen (`1.2.0-rc01`) are marked pre-release.
- **Firebase App Distribution** — needs `FIREBASE_DISTRIBUTION_CREDENTIAL_JSON`.
  Tester lists live in `.distribution/`.
- **Google Drive** (APK + AAB) — needs all four of
  `GG_DRIVE_CLIENT_ID`, `GG_DRIVE_CLIENT_SECRET`, `GG_DRIVE_REFRESH_TOKEN`, `GG_DRIVE_FOLDER_ID`.
- **Discord** — needs `DISCORD_WEBHOOK`. Set the `DISCORD_MENTION` variable to prefix a role ping.

See [CI_CD_SETUP.md](CI_CD_SETUP.md) for the full secret/variable table.

## Security notes

- All credentials are repository secrets; the keystore is base64-encoded and decoded at runtime.
- `.gitignore` excludes `*.jks`, `*.keystore`, `keystore.properties`, `*.apk`, `*.aab`, `.env`.
  Never commit signing material or tokens — put them in Actions secrets.
- Only the AAB from a hyphen-free production tag should go to the Play Store. RC builds
  carry an explicit warning in their Discord notification.
