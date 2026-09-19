# Quy tắc code cho AI coding agent

> File này dành cho **mọi AI coding tool** làm việc trên repo này: Claude Code, OpenAI Codex,
> Cursor, Gemini CLI, Antigravity, Copilot… Codex/Cursor/Antigravity tự đọc `AGENTS.md` ở gốc
> project; Claude Code đọc `CLAUDE.md` (file đó trỏ về đây).
>
> Đây là **template** — file này được đồng bộ xuống mọi app tạo từ nó
> (`.templatesyncignore` không loại trừ nó). Sửa rule ở template, không sửa rời ở từng app.
>
> Dev: nếu AI làm sai một trong các rule dưới đây, **reject PR** — đừng sửa tay rồi cho qua,
> lần sau nó lại sai y hệt. Rule sai/thiếu thì sửa file này.

## 0. Nguyên tắc nền

1. **Bắt chước code có sẵn, đừng sáng tạo.** Trước khi viết màn mới, mở một màn cùng loại đang
   có (`presentation/language/`, `presentation/welcomeback/`) và copy đúng cấu trúc đó.
2. **Không tự ý đổi kiến trúc, không thêm thư viện mới** khi chưa được yêu cầu. Cần thêm
   dependency → hỏi trước, và khai qua `gradle/libs.versions.toml` (version catalog), không
   hardcode version trong `build.gradle.kts`.
3. **Mọi file `.kt` mới phải có license header** giống hệt các file hiện có:
   ```kotlin
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
   ```
4. **Comment giải thích TẠI SAO, không phải ĐANG LÀM GÌ.** Codebase này viết comment tiếng Việt
   cho những chỗ có bẫy (xem `LanguageViewModel`, `BaseActivity.onCreate`). Code hiển nhiên thì
   không cần comment.

## 1. File phải nằm đúng thư mục

Package gốc: `com.onetabb.<app>.android` (template: `com.onetabb.template.android`).
Package của file **phải khớp đường dẫn thư mục** — không có ngoại lệ.

| Loại file | Nơi đặt | Ví dụ |
| :-- | :-- | :-- |
| Activity / Fragment / ViewModel của 1 màn | `presentation/<feature>/` | `presentation/language/LanguageActivity.kt` |
| Adapter của màn đó | `presentation/<feature>/adapter/` | `presentation/language/adapter/LanguageAdapter.kt` |
| Model/UI-model của màn đó | `presentation/<feature>/model/` | `presentation/language/model/LanguageItem.kt` |
| Dialog / BottomSheet dùng chung | `presentation/dialog/` | `presentation/dialog/NetworkDialog.kt` |
| Base class | `core/base/` | `core/base/BaseActivity.kt` |
| Extension function | `core/extensions/` | `core/extensions/ViewEx.kt` |
| Custom view | `core/widget/` | |
| Util thuần logic | `core/utils/` | `core/utils/NetworkTracker.kt` |
| Xin quyền | `core/permission/` | |
| Đọc/ghi dữ liệu (SharedPref, DB, API) | `data/local/`, `data/remote/` | `data/local/SharedPref.kt` |
| Nguồn dữ liệu tĩnh | `data/provider/` | `data/provider/LanguageProvider.kt` |
| Hilt module | `di/` | `di/AppModule.kt` |
| Mọi thứ liên quan quảng cáo | `ads/` | `ads/AdsIds.kt` |
| Remote Config | `util/remoteconfig/` | |

**Sai thường gặp:** nhét adapter/model vào cùng file Activity; tạo package `utils` thứ hai ở
gốc; đặt file data trong `presentation/`. Tất cả đều bị reject.

Đặt tên: `XxxActivity` / `XxxFragment` / `XxxViewModel` / `XxxAdapter` / `XxxDialog`.
Layout: `activity_xxx.xml`, `fragment_xxx.xml`, `dialog_xxx.xml`, `item_xxx.xml`,
`view_xxx.xml`, `layout_xxx.xml`.

## 2. Không hardcode

**Cấm** chuỗi/màu/kích thước/số ma thuật viết thẳng trong code Kotlin hoặc XML.

| Thứ | Để ở đâu |
| :-- | :-- |
| Text người dùng đọc được | `res/values/strings.xml` (thêm ngôn ngữ → `res/values-<code>/strings.xml`) |
| Màu | `res/values/colors.xml` |
| Khoảng cách, cỡ chữ | `res/values/dimens.xml` |
| Key `SharedPreferences`, tên extra của `Intent`, tên action | `private const val` trong `companion object` của chính class đó |
| Screen id / trigger id / slot id của ads | `ads/AdsIds.kt` (`AdsScreen`, `AdsTrigger`, `AdsSlot`) |
| Endpoint, flag build | `BuildConfig` qua `build.gradle.kts` |

```kotlin
// ❌ SAI
binding.tvTitle.text = "Chọn ngôn ngữ"
if (type == 1) { ... }
intent.getStringExtra("user_id")

// ✅ ĐÚNG
binding.tvTitle.text = getString(R.string.language_title)
if (type == TYPE_PREMIUM) { ... }
intent.getStringExtra(EXTRA_USER_ID)
```

Ngoại lệ duy nhất: log/tag debug, và chuỗi kỹ thuật không bao giờ hiện cho user (tên key JSON,
tên param Firebase). Vẫn nên đưa vào `const val`.

## 3. Activity/Fragment chỉ là tầng view

Activity/Fragment **chỉ được** làm 4 việc: inflate binding, gắn listener, quan sát state, và
render state lên view. Mọi thứ khác là của ViewModel.

Cấm trong Activity/Fragment: gọi API/DB/SharedPref trực tiếp, tính toán nghiệp vụ, giữ state
(ngoài state thuần UI như vị trí scroll), `if/else` phân nhánh nghiệp vụ, khởi tạo bằng `new`
những thứ đáng lẽ phải inject.

Dùng đúng 3 hook của base class, **không override `onCreate`/`onViewCreated`**:

```kotlin
@AndroidEntryPoint
class XxxActivity : BaseActivity<ActivityXxxBinding>() {

    private val viewModel: XxxViewModel by viewModels()

    override fun inflateBinding() = ActivityXxxBinding.inflate(layoutInflater)

    override val adsScreenId = AdsScreen.XXX_VIEW

    override fun initConfig(savedInstanceState: Bundle?) { /* đọc intent, set adapter */ }

    override fun initListener() { /* setOnClickListener → gọi viewModel.xxx() */ }

    override fun initObserver() {
        viewModel.uiState.collectWithLifecycle(this) { state -> render(state) }
    }
}
```

## 4. ViewModel

- **Màn có state, gọi data, hoặc có logic nghiệp vụ → BẮT BUỘC có ViewModel.**
- Màn thuần tĩnh (chỉ hiển thị nội dung cố định, ví dụ `OnboardingPageFragment`,
  `TabPlaceholderFragment`) được phép không có ViewModel. **Nhưng ngay khi màn đó phát sinh
  state hoặc phải đọc/ghi dữ liệu thì phải tách ViewModel ngay trong chính lần sửa đó**, không
  để dồn.
- Không tạo ViewModel rỗng chỉ để "cho đủ rule".
- ViewModel **không được** biết gì về Android UI: cấm import `Activity`, `Fragment`, `View`,
  `Context` (cần context → `BaseAndroidViewModel` hoặc inject `@ApplicationContext`).
- State đưa ra ngoài bằng `StateFlow`/`SharedFlow` (dùng `UiState<T>` trong `core/base/` khi có
  loading/error), không dùng `LiveData` mới, không để field `public var`.
- Kế thừa `BaseViewModel` khi ViewModel có coroutine (để dùng `launchSafely`, đã gắn sẵn
  `CoroutineExceptionHandler`); cần `Application` context thì `BaseAndroidViewModel`. ViewModel
  thuần đồng bộ, không coroutine, thì extend thẳng `ViewModel` (so sánh `LanguageViewModel` với
  `LaunchViewModel`). **Cấm `GlobalScope` trong mọi trường hợp.**

## 5. Dependency Injection — Hilt

- App dùng **Hilt**. Cấm tự khởi tạo dependency, cấm `object Singleton` thủ công giữ state, cấm
  service locator tự chế.
- Activity/Fragment cần inject → `@AndroidEntryPoint`. ViewModel → `@HiltViewModel` +
  `@Inject constructor`.
- Binding mới khai trong `di/AppModule.kt` (hoặc module riêng trong `di/` nếu module đã to).
- Dependency có I/O phải là **interface + implementation**, inject interface:
  `SharedPref` (interface) ← `SharedPreferencesImpl`. Không inject thẳng class cụ thể.

## 6. View & resource

- Dùng **ViewBinding** (`binding.xxx`). Cấm `findViewById`, cấm Kotlin synthetics.
- Trong Fragment chỉ chạm `binding` giữa `onCreateView` và `onDestroyView` — base class đã lo
  vòng đời, đừng tự giữ tham chiếu view.
- Không tự viết lại thứ `core/base/` và `core/extensions/` đã có: `BaseListAdapter`,
  `BaseDiffCallback`, `collectWithLifecycle`, các file `*Ex.kt`. Đọc trước khi viết mới.

## 7. Quảng cáo (AdsHub)

- `ads/AdsIds.kt` và `app/src/main/assets/ads_config.json` là **một hợp đồng hai chiều** — sửa
  bên này phải sửa bên kia.
- **Mọi màn** đều khai `adsScreenId` và được bind, kể cả màn không có quảng cáo.
- **Mọi nút bấm** đi qua `triggerAds(AdsTrigger.XXX) { ... }`, kể cả nút chưa có plan ads
  (trigger để `enabled: false` trong config, vẫn đếm ngầm).
- Điều hướng sau khi đóng ads luôn bọc `runWhenResumed { }`.
- Chi tiết đầy đủ: skill `adshub-integrate` trong `.claude/skills/adshub-integrate/`
  (`AGENTS.md` + `AI_INTEGRATION_PLAYBOOK.md` trong thư mục đó).

## 8. Không được đụng vào

- **Secret trên máy dev:** `local.properties`, `~/.gradle/gradle.properties`,
  `keystore.properties`, `.env`. Thấy `gpr.user`/`gpr.key` hay key bất kỳ trong đó thì **đọc để
  biết là đã có auth rồi để nguyên** — không xoá, không "dọn giúp", không in giá trị ra
  báo cáo/log. Các file này đã gitignore, không bao giờ lên repo.
- Token/khoá trong file **git đang theo dõi** → báo cho người dùng và chờ họ quyết, không tự xoá.
- `.github/workflows/**`, `.templatesyncignore`, `gradle/wrapper/**`: chỉ sửa khi được yêu cầu
  đích danh — chúng đồng bộ từ template và ảnh hưởng cả pipeline phát hành.
- `gradle/libs.versions.toml`: không tự nâng version. Renovate lo việc đó.

## 9. Checklist trước khi báo xong

Không được nói "đã xong" nếu chưa qua hết:

- [ ] `./gradlew :app:assembleDebug` xanh (Windows: `.\gradlew.bat`). Đỏ thì sửa tới khi xanh.
- [ ] File mới nằm đúng thư mục ở mục 1, package khớp đường dẫn, có license header.
- [ ] Không còn chuỗi/màu/số ma thuật hardcode trong code vừa viết.
- [ ] Activity/Fragment vừa sửa không chứa logic nghiệp vụ; state nằm ở ViewModel.
- [ ] Dependency mới đi qua Hilt, không `new` thủ công.
- [ ] Màn mới: đã khai `AdsScreen`, đã `adsScreenId`, nút mới đã có `AdsTrigger` và đã thêm vào
      `ads_config.json`.
- [ ] Báo cáo rõ: file đã tạo/sửa, phần nào chưa làm được và vì sao. Không báo xong kiểu chung
      chung khi còn TODO.
