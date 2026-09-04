# AdsHub Integration Kit

Bộ kit **tự chứa** để AI tích hợp AdsHub SDK vào project Android từ A→Z. Không cần clone repo AdsHub — chỉ cần thư mục này + token GitHub Packages (1Tabb cấp) để gradle tải AAR.

## Cài theo tool AI bạn dùng

| Tool | Cách cài |
| :-- | :-- |
| **Claude Code** | Copy CẢ thư mục kit này thành `C:\Users\<user>\.claude\skills\adshub-integrate\` (mọi project trên máy dùng được). Xong — nhắc đến "AdsHub" là skill tự kích hoạt. |
| **OpenAI Codex** | Copy kit vào project (vd `adshub-kit/`); append nội dung `AGENTS.md` của kit vào `AGENTS.md` ở gốc project (sửa đường dẫn kit). |
| **Google Antigravity** | Copy kit vào project; paste nội dung `AGENTS.md` vào Customizations → Rules (hoặc để `AGENTS.md` ở gốc project). |
| **Tool khác / không cài gì** | Prompt thẳng: `Đọc <đường-dẫn-kit>/AI_INTEGRATION_PLAYBOOK.md và tích hợp AdsHub theo brief sau: <paste Bảng Ads>` |

## Cách dùng (mọi tool như nhau)

Paste **Bảng Ads** từ Google Sheet của AdOps (dòng `APP ID:` + các cột Ad name/Key remote/Loại Ads/ID Admob/Mô tả) kèm câu *"Tích hợp AdsHub theo brief này"*. AI tự chạy 8 phase đến khi build xanh. Chi tiết: xem doc Word `AdsHub_Skill_AI_HuongDan.docx` đi kèm kit.

## File trong kit

| File | Vai trò |
| :-- | :-- |
| `SKILL.md` | Entry cho Claude Code |
| `AGENTS.md` | Entry cho Codex / Antigravity / tool khác |
| `AI_INTEGRATION_PLAYBOOK.md` | Quy trình 8 phase — nguồn sự thật |
| `INTEGRATION_BRIEF_TEMPLATE.md` | Format brief người dùng cung cấp |
| `ads_config.template.json` | Template config plan chuẩn 1Tabb |
| `AdsBootstrap.template.kt` | Wrapper duy nhất giữa app và SDK |
| `AdsBaseHelpers.template.kt` | BaseAdsActivity/BaseAdsFragment hoàn chỉnh |
| `JSON_SETUP_GUIDE.md` | Từ điển đầy đủ field của ads_config.json |
| `ADSHUB_DEV_GUIDE.md` | API chi tiết + code mẫu Compose (2 mục lỗi thời — playbook là chuẩn) |

Điều kiện duy nhất phía máy dev: `~/.gradle/gradle.properties` có `gpr.user`/`gpr.key` (token GitHub scope `read:packages` do 1Tabb cấp).
