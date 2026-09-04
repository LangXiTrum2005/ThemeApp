# Hướng dẫn cấu hình Secrets, Variables và Labels

Làm theo thứ tự. Nhóm 0–3 là bắt buộc để pipeline chạy; nhóm 4–6 là tuỳ chọn,
thiếu secret thì job tự skip chứ không làm đỏ pipeline.

## Tổng quan

| Nhóm | Tên | Bắt buộc | Đặt ở |
|---|---|---|---|
| 1 | `ANDROID_KEYSTORE_BASE64` | ✅ | repo |
| 1 | `ANDROID_KEY_ALIAS` | ✅ | repo |
| 1 | `ANDROID_KEYSTORE_PASSWORD` | ✅ | repo |
| 1 | `ANDROID_KEY_PASSWORD` | ✅ | repo |
| 2 | `GH_PAT` | ✅ | repo |
| 3 | 3 label `release` / `tracking` / `release-drafter` | ✅ | repo |
| 4 | `FIREBASE_DISTRIBUTION_CREDENTIAL_JSON` | ⬜ | repo |
| 4 | `GOOGLE_SERVICES_JSON` | ⬜ | repo |
| 5 | `DISCORD_WEBHOOK` | ⬜ | repo |
| 6 | `GG_DRIVE_CLIENT_ID` / `_CLIENT_SECRET` / `_REFRESH_TOKEN` / `_FOLDER_ID` | ⬜ | repo |
| — | `VERSION_CODE_OFFSET` | ⬜ | repo (Variables) |

---

## ⚠️ Đọc trước: vì sao mọi thứ để ở cấp repo

Org đang dùng **GitHub Free**. Tài liệu GitHub ghi rõ:

> Organization-level secrets and variables are not accessible by private repositories
> for GitHub Free.

Repo này là **private**, nên **mọi org secret đều vô hình** với nó — biểu hiện là secret
trả về chuỗi rỗng, và workflow báo lỗi kiểu `Input required and not supplied: token`.

Vì vậy toàn bộ hướng dẫn dưới đây dùng **repository secret**:

```
https://github.com/<org>/<repo>/settings/secrets/actions
```

Muốn dùng chung secret cho nhiều project thì phải nâng org lên **Team**, hoặc để repo
**public**.

---

# Nhóm 0 — Quyền Actions

Vào Settings → Actions → General → **Workflow permissions**:

- Chọn **Read and write permissions**
- Tick **Allow GitHub Actions to create and approve pull requests**

**Nếu hai ô này bị xám không bấm được**, nghĩa là org đang khoá. Sửa ở cấp org trước:

```
https://github.com/organizations/<org>/settings/actions
```

Đặt Workflow permissions ở đó thành Read and write, rồi quay lại trang repo.

---

# Nhóm 1 — Signing (bắt buộc)

## 1.1 Tạo keystore

```bash
keytool -genkeypair -v -keystore release.jks -storetype PKCS12 -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

Trả lời các câu hỏi; nhớ **ghi lại mật khẩu**.

PKCS12 không hỗ trợ mật khẩu store và key khác nhau, nên
`ANDROID_KEYSTORE_PASSWORD` và `ANDROID_KEY_PASSWORD` sẽ là **cùng một giá trị**.

`-validity 10000` ≈ 27 năm, đúng khuyến nghị của Google. Key hết hạn thì không update
app trên Play Store được nữa.

> **Backup `release.jks` ra nơi an toàn.** Mất file này là mất luôn khả năng update app
> trên Play Store — Google không khôi phục được.

## 1.2 Encode base64

```bash
base64 -w0 release.jks | clip
```

`-w0` là bắt buộc. Thiếu nó, `base64` xuống dòng mỗi 76 ký tự và CI decode hỏng.

## 1.3 Tạo 4 secret

| Name | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Ctrl+V chuỗi base64 (một dòng dài) |
| `ANDROID_KEY_ALIAS` | `upload` |
| `ANDROID_KEYSTORE_PASSWORD` | mật khẩu keystore |
| `ANDROID_KEY_PASSWORD` | cùng mật khẩu |

Bốn cái này **luôn để ở repo**, kể cả sau này lên gói Team — mỗi app một keystore riêng.

---

# Nhóm 2 — PAT (bắt buộc)

`GH_PAT` dùng cho hai việc mà `GITHUB_TOKEN` mặc định không làm
được: push tag sao cho tag đó kích hoạt được workflow khác, và dispatch `ci.yml` sao cho
run đó phát ra sự kiện `workflow_run`.

## 2.1 Tạo token

```
https://github.com/settings/tokens/new
```

| Trường | Điền |
|---|---|
| Note | `tag-on-pr-comment` |
| Expiration | `90 days` / `1 year` / `No expiration` |

Scope — tick đúng **2 ô**:

- ☑️ **`repo`** (tick ô cha, các ô con tự tick theo)
- ☑️ **`workflow`**

Bấm **Generate token**. Chuỗi `ghp_...` **chỉ hiện một lần**, copy ngay.

> Token hết hạn thì CI đứng im với triệu chứng khó đoán: tag tạo ra nhưng không có gì
> chạy tiếp. Nếu chọn thời hạn, đặt lịch nhắc.

## 2.2 Authorize SSO (nếu org bật)

Nếu bên cạnh token có nút **Configure SSO** → bấm → chọn org của bạn → **Authorize**.

Bỏ qua bước này thì token trông hợp lệ nhưng mọi thao tác lên repo của org trả
`404 Not Found` — lỗi rất dễ hiểu nhầm thành sai đường dẫn.

## 2.3 Lưu secret

| Name | `GH_PAT` |
|---|---|
| Secret | chuỗi `ghp_...` |

Token mang quyền của **chính bạn**, nên tài khoản bạn phải có quyền `write` trở lên
trên repo.

---

# Nhóm 3 — Labels (bắt buộc)

```
https://github.com/<org>/<repo>/labels
```

## 3.1 Ba label bắt buộc

Tên phải chính xác, phân biệt hoa thường:

- `release`
- `tracking`
- `release-drafter`

`release-branch-creator.yml` gắn cả ba lên tracking PR ngay sau khi tạo — thiếu một cái
là job đỏ. `release-drafter.yml` chỉ chạy nếu PR mang ít nhất một trong ba.

## 3.2 Chín label cho changelog (nên có)

`feature` · `enhancement` · `fix` · `bug` · `documentation` · `dependencies` ·
`major` · `breaking` · `skip-changelog`

Release-drafter dùng chúng để phân nhóm release notes. PR không gắn label vẫn xuất hiện,
chỉ là rơi vào mục *Other Changes*. Gắn `skip-changelog` thì ẩn khỏi release notes.

---

# Nhóm 4 — Firebase App Distribution (tuỳ chọn)

Không có secret này thì job `upload_firebase` skip.

## 4.1 Tạo service account

1. https://console.cloud.google.com → chọn project Firebase của app
2. **IAM & Admin** → **Service Accounts** → **Create service account**
3. Name: `firebase-distributor` → **Create and continue**
4. Role: chọn **Firebase App Distribution Admin** → **Continue** → **Done**

## 4.2 Tạo key JSON

Bấm vào service account vừa tạo → tab **Keys** → **Add key** → **Create new key** →
**JSON** → tải về.

## 4.3 Lưu secret

| Name | `FIREBASE_DISTRIBUTION_CREDENTIAL_JSON` |
|---|---|
| Secret | dán **toàn bộ** nội dung file JSON (không base64) |

Xong thì xoá file JSON khỏi máy — đó là credential.

## 4.4 `GOOGLE_SERVICES_JSON` (không bắt buộc)

`app/google-services.json` đã commit trong repo, nên Firebase app id được đọc trực tiếp
từ đó. Chỉ thêm secret này nếu muốn CI ghi đè file lúc build.

| Name | `GOOGLE_SERVICES_JSON` |
|---|---|
| Secret | nội dung `app/google-services.json` |

File JSON phải chứa **2 client**: package chính (build `release`) và package `.dev`
(build `debug`, do `applicationIdSuffix = ".dev"` trong `app/build.gradle.kts`). Thiếu
client `.dev` thì build debug sẽ fail ở task `processDebugGoogleServices`.

Cách lấy: Firebase Console → Project settings → *Add app* → Android → nhập
`<applicationId>.dev` → tải lại `google-services.json` (file mới sẽ có sẵn cả 2 client).
Job Firebase Distribution luôn lấy app id của client **không** kết thúc bằng `.dev`.

## 4.5 Danh sách tester

Sửa trong repo, không phải secret:

- `.distribution/testers.txt` — email tester, phân cách bằng dấu phẩy.
  **Không để dấu phẩy thừa cuối** (sinh entry rỗng, CLI có thể coi là email hỏng).
- `.distribution/tester_groups.txt` — **alias** của group trong Firebase Console.

Hành vi của Firebase CLI với 2 file này **khác nhau**, đừng nhầm:

| File | CLI có tự tạo không |
|---|---|
| `testers.txt` | ✅ Email lạ được tự thêm vào project và gắn vào release |
| `tester_groups.txt` | ❌ Group phải **tồn tại sẵn** trên console. Alias không có → bước gắn tester chết cả cụm: release vẫn hiện trên console nhưng **không ai nhận mail** |

## 4.6 Việc trên Firebase Console (một lần cho mỗi app)

1. **Bật App Distribution**: Release & Monitor → App Distribution → chọn app →
   **Get started**. Chưa bấm thì lần upload đầu fail dù credential đúng.
2. **Tạo group**: tab Testers & Groups → **Add group** → đặt tên sao cho **alias**
   (hiện ngay dưới ô tên) trùng chính xác nội dung `tester_groups.txt` →
   add email tester vào group.

## 4.7 Về email mời tester

- Mail chỉ gửi khi tester được **gắn vào release** — thêm vào project/group thôi
  là chưa có mail. Mail Firebase hay rơi vào **Spam/Promotions**.
- Tester lần đầu phải bấm **Accept invitation**; từ đó bản mới tự hiện trong
  app App Tester.
- Bước upload đang có `continue-on-error` — **CI xanh không có nghĩa upload ổn**.
  Không thấy mail thì mở log step "Upload to Firebase Distribution" đọc lỗi thật.
- File **AAB** chỉ phát được khi Firebase project đã **link Google Play**
  (Project settings → Integrations). Chưa link thì AAB fail lặng lẽ, 2 APK vẫn
  phát bình thường.

---

# Nhóm 5 — Discord (tuỳ chọn)

Không có secret này thì mọi bước Discord skip.

## 5.1 Tạo webhook

Cần quyền **Manage Webhooks** trên server.

1. Chuột phải vào kênh muốn nhận thông báo → **Edit Channel**
2. **Integrations** → **Webhooks** → **New Webhook**
3. Đặt tên (vd `CI Bot`), chọn kênh
4. **Copy Webhook URL**

## 5.2 Test trước khi lưu

```bash
curl -H "Content-Type: application/json" -d '{"content":"test tu CI"}' "DAN_URL_VAO_DAY"
```

Thấy tin nhắn trong kênh là webhook chuẩn.

## 5.3 Lưu secret

| Name | `DISCORD_WEBHOOK` |
|---|---|
| Secret | URL webhook |

URL này **là credential** — ai có nó đều đăng được tin vào kênh của bạn.

## 5.4 Bạn sẽ nhận được gì

| Khi nào | Nội dung |
|---|---|
| Vừa tạo tag | 🔨 Building `<version>` (vàng) |
| CI thành công | 🚀 Release `<version>` Complete (xanh) — kèm link Firebase, release notes |
| CI hỏng | ❌ Release `<version>` Failed (đỏ) — kèm link log |

Tag có dấu gạch ngang (`1.0.0-rc01`) sẽ kèm cảnh báo không dùng để publish Play Store.

> `release-tag.yml` đang hardcode `content: "@Android"` ở 3 chỗ (dòng 105, 251, 264).
> Discord chỉ ping khi dùng ID số. Muốn ping thật: bật Developer Mode trong Discord →
> chuột phải role → Copy Role ID → thay thành `content: "<@&ROLE_ID>"`.

---

# Nhóm 6 — Google Drive (tuỳ chọn)

> `job-drive.yml` upload bằng **OAuth refresh token của chính tài khoản bạn**, gọi
> thẳng Drive API. Làm đủ mục này là chạy, không phải sửa workflow. Job upload toàn
> bộ **APK + AAB** (tên file kèm prefix tag) — PO vào thư mục lấy AAB đẩy Play Console.
>
> Sau này công ty có Google Workspace: làm lại mục 6.2–6.7 bằng tài khoản công ty rồi
> thay giá trị 4 secret là xong — workflow đã hỗ trợ sẵn cả Shared Drive
> (`supportsAllDrives=true`), không cần sửa gì thêm.

## 6.1 Vì sao service account không dùng được

Service account **không có dung lượng lưu trữ** (0 byte). Khi nó upload vào "My Drive"
của bạn, file thuộc sở hữu của **nó**, không phải bạn — nên Google chặn với:

```
Error 403: Service Accounts do not have storage quota., storageQuotaExceeded
```

Cấp quyền Editor cho thư mục **không sửa được** điều này: quyền và quota là hai chuyện
khác nhau. Nó có chìa khoá vào phòng nhưng không có chỗ đặt đồ.

Google gợi ý dùng **Shared Drive** hoặc **OAuth delegation** — cả hai đều là tính năng
**Google Workspace**, tài khoản Gmail cá nhân không có.

Cách khả thi với Gmail cá nhân: dùng **OAuth của chính tài khoản bạn**. File thuộc sở
hữu của bạn và dùng 15 GB quota của bạn.

## 6.2 Bật Drive API + cấu hình Google Auth Platform

> Google đã thay màn "OAuth consent screen" cũ bằng **Google Auth Platform** —
> các bước dưới viết theo giao diện mới.

1. **Bật API**: Cloud Console → APIs & Services → **Library** → tìm
   `Google Drive API` → **Enable**. Thiếu bước này CI lỗi `403 accessNotConfigured`.
2. **Google Auth Platform** → nếu hiện "not yet configured" bấm **First steps**:
   - App name: `Drive Uploader`; support email: Gmail của bạn → Next
   - Audience: **External** → Next
   - Contact email → Next → đồng ý → **Create**

## 6.3 Publish app — quan trọng nhất

Menu trái **Audience** → Publication status đang **Testing** → bấm
**Publish the application** → xác nhận.

Để nguyên chế độ Testing thì **refresh token hết hạn sau 7 ngày**, CI chết mỗi
tuần. Publish rồi token sống vô thời hạn. Không cần verify app — cảnh báo
"unverified" lúc đăng nhập vượt bằng **Advanced → Go to Drive Uploader (unsafe)**
(app của chính bạn nên không sao).

## 6.4 Tạo OAuth client

Menu trái **Clients** → **Create client**

- Application type: **Desktop app**
- Name: `ci-uploader` → **Create**

Copy **Client ID** và **Client secret** ngay tại dialog vừa hiện —
**secret chỉ hiện đầy đủ đúng một lần lúc tạo**. Lỡ đóng mất: vào **Clients** →
bấm vào client → mục **Client secrets** → **Add secret** sinh secret mới
(Client ID giữ nguyên).

## 6.5 Lấy refresh token

Mở link sau trong trình duyệt, thay `CLIENT_ID_CUA_BAN`:

```
https://accounts.google.com/o/oauth2/v2/auth?client_id=CLIENT_ID_CUA_BAN&redirect_uri=http://localhost&response_type=code&scope=https://www.googleapis.com/auth/drive&access_type=offline&prompt=consent
```

Đăng nhập → qua cảnh báo → trình duyệt chuyển tới `http://localhost/?code=4/0AX...`
(trang báo lỗi không truy cập được là bình thường). Copy đoạn sau `code=` trên thanh
địa chỉ.

Đổi code lấy refresh token:

```bash
curl -X POST https://oauth2.googleapis.com/token -d client_id=CLIENT_ID -d client_secret=CLIENT_SECRET -d code=CODE_VUA_COPY -d grant_type=authorization_code -d redirect_uri=http://localhost
```

JSON trả về có `"refresh_token": "1//0g..."`.

## 6.6 Lấy folder ID

Mở thư mục Drive, lấy đoạn sau `/folders/` trên thanh địa chỉ:

```
https://drive.google.com/drive/folders/1a2B3cD4eF5gH6iJ7kL9mN
                                        └────────── ID ──────┘
```

## 6.7 Bốn secret

| Name | Value |
|---|---|
| `GG_DRIVE_CLIENT_ID` | Client ID bước 6.4 |
| `GG_DRIVE_CLIENT_SECRET` | Client secret bước 6.4 |
| `GG_DRIVE_REFRESH_TOKEN` | refresh_token bước 6.5 |
| `GG_DRIVE_FOLDER_ID` | folder ID bước 6.6 |

Xoá `GG_DRIVE_CREDENTIAL_JSON_BASE64` nếu đã tạo — không dùng nữa.

## 6.8 Workflow

Đã áp dụng: `job-drive.yml` đổi refresh token lấy access token rồi upload APK/AAB qua
Drive API (resumable upload). Refresh token hỏng (thường do quên PUBLISH APP ở mục
6.3) thì job fail với log chỉ rõ nguyên nhân — không fail lặng lẽ.

---

# Variables (tab riêng, không phải Secrets)

```
https://github.com/<org>/<repo>/settings/variables/actions
```

| Name | Value | Ghi chú |
|---|---|---|
| `VERSION_CODE_OFFSET` | `0` | Công thức: `versionCode = github.run_number + offset` |

Nếu app đã có trên Play Store với versionCode ví dụ 47, đặt offset ≥ 47 — Play Store từ
chối bản upload có versionCode nhỏ hơn hoặc bằng bản đã có.

Không tạo variable này cũng chạy, `ci.yml` mặc định về `0`.

Hai variable `TEMPLATE_SOURCE_REPO` và `TEMPLATE_SOURCE_BRANCH` chỉ cần khi dùng template
sync — xem [TEMPLATE_SYNC_SETUP.md](TEMPLATE_SYNC_SETUP.md).

---

# Checklist

Tối thiểu để chạy `/build rc`:

- [ ] Workflow permissions = Read and write (kiểm cả cấp org nếu bị xám)
- [ ] 4 secret ký (nhóm 1): `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEY_ALIAS`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_PASSWORD`
- [ ] `GH_PAT`
- [ ] 3 label `release` / `tracking` / `release-drafter`

Thêm cho đầy đủ:

- [ ] `FIREBASE_DISTRIBUTION_CREDENTIAL_JSON` + `.distribution/*.txt`
- [ ] `DISCORD_WEBHOOK`
- [ ] `VERSION_CODE_OFFSET`

---

# Mở repo app mới — dùng lại gì, tạo mới gì

GitHub Free không share được secret cấp org, nên repo mới phải **dán lại tất cả**
— nhưng chỉ một phần phải *tạo mới*:

| Secret / việc | Repo app mới |
|---|---|
| `GH_PAT` | Dán lại giá trị cũ |
| `DISCORD_WEBHOOK` | Dán lại nếu báo chung kênh; app riêng kênh thì tạo webhook mới |
| `GG_DRIVE_CLIENT_ID` / `_CLIENT_SECRET` / `_REFRESH_TOKEN` | Dán lại giá trị cũ — phần OAuth (nhóm 6) chỉ làm **một lần trong đời** |
| `GG_DRIVE_FOLDER_ID` | **Mới** — tạo folder Drive cho app, lấy ID sau `/folders/` |
| `ANDROID_KEYSTORE_BASE64` / `ANDROID_KEY_ALIAS` / `ANDROID_KEYSTORE_PASSWORD` / `ANDROID_KEY_PASSWORD` | **Mới** — mỗi app một keystore riêng (nhóm 1) |
| `FIREBASE_DISTRIBUTION_CREDENTIAL_JSON` | **Mới** — mỗi app một Firebase project → service account mới (nhóm 4) |
| Variable `VERSION_CODE_OFFSET` | **Mới** — theo versionCode app đang có trên Play |
| 3 label + Workflow permissions | Làm lại theo nhóm 0 và 3 |
| `.distribution/*.txt`, `google-services.json`, `applicationId` | Sửa theo app |
| Firebase Console: Get started + tạo group | Làm lại theo mục 4.6 |

> ⚠️ GitHub **không cho xem lại** giá trị secret đã lưu. Các giá trị dùng lại
> (PAT, bộ 3 Drive OAuth) phải cất vào password manager ngay từ lần tạo đầu,
> không thì đến app thứ hai phải đi làm lại từ đầu.

## Thời hạn của từng key

| Key | Thời hạn | Chết vì / triệu chứng |
|---|---|---|
| `GH_PAT` | ⏰ theo lựa chọn lúc tạo | Duy nhất chết theo lịch. Triệu chứng lặng lẽ: "tag tạo ra nhưng không gì chạy tiếp" — đặt lịch nhắc gia hạn |
| `GG_DRIVE_REFRESH_TOKEN` | ♾️ có điều kiện | App còn ở chế độ Testing (chết sau 7 ngày); không được dùng suốt 6 tháng; tự revoke trong tài khoản Google |
| Client ID/Secret, service account key, webhook, folder ID | ♾️ | Chỉ chết khi tự tay xóa |
| Keystore | ~27 năm | Hết hạn là hết đường update app trên Play (chuyện của ~2053) |

---

# Lỗi thường gặp

**`Input required and not supplied: token`**
`GH_PAT` rỗng. Khả năng cao nhất: secret đang để ở **cấp org** mà
repo là private trên gói Free. Tạo lại ở cấp repo.

**`404 Not Found` khi thao tác repo của org**
PAT chưa Authorize SSO (bước 2.2).

**Tag tạo được nhưng không có gì chạy tiếp**
PAT hết hạn, hoặc thiếu scope `workflow`.

**Ô Workflow permissions bị xám**
Org đang khoá. Sửa ở `https://github.com/organizations/<org>/settings/actions`.

**Build lỗi lúc decode keystore**
Chuỗi base64 bị xuống dòng. Encode lại bằng `base64 -w0`.

**`403 storageQuotaExceeded` khi upload Drive**
Service account không có quota — xem mục 6.1.

**`403 accessNotConfigured` khi gọi Google API**
Chưa bật API tương ứng trong **APIs & Services → Library**.

**Release hiện trên Firebase console nhưng tester không nhận mail**
Group trong `tester_groups.txt` chưa tồn tại trên console (mục 4.5–4.6) — CLI
upload binary xong rồi chết ở bước gắn tester. CI vẫn xanh vì `continue-on-error`;
lỗi thật nằm trong log step "Upload to Firebase Distribution". Sửa xong có thể gắn
group thủ công vào release sẵn có để mail đi ngay, không cần build lại.

**APK phát được cho tester nhưng AAB thì không**
Firebase project chưa link Google Play (Project settings → Integrations). AAB fail
lặng lẽ, không ảnh hưởng APK.
