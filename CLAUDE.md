# CLAUDE.md

## Đọc trước khi viết bất kỳ dòng code nào

**[AGENTS.md](AGENTS.md) là nguồn sự thật duy nhất về quy tắc code của repo này** — cấu trúc thư
mục, cấm hardcode, tầng view/ViewModel, Hilt, quy ước ads, checklist trước khi báo xong. Đọc hết
file đó rồi mới bắt tay làm.

Đừng chép lại rule sang file này: một rule chỉ được ghi ở một chỗ, nếu không hai bản sẽ lệch nhau.
Rule sai hoặc thiếu → sửa `AGENTS.md`.

## Riêng cho Claude Code

- **Skill `adshub-integrate`** (`.claude/skills/adshub-integrate/`) chứa toàn bộ tri thức tích hợp
  quảng cáo AdsHub: playbook A→Z, template `ads_config.json`, từ điển field config, bẫy thường
  gặp. Đụng tới bất cứ thứ gì liên quan quảng cáo thì gọi skill đó trước, đừng tự suy luận.
- Thư mục `.claude/` **không** đồng bộ từ template (`.templatesyncignore` loại trừ nó), nên skill
  ở đây là bản riêng của project này. `AGENTS.md` và `CLAUDE.md` thì có đồng bộ.

## Build

```bash
./gradlew :app:assembleDebug
```

Trên Windows dùng `.\gradlew.bat`. Build phải xanh mới được báo xong.
