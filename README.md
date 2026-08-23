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
./gradlew :app:assembleRelease :app:bundleRelease -PciVersionCode=42 -PciVersionName=1.2.0-rc01 -PSIGNING_KEY_ALIAS=... -PSIGNING_KEY_STORE_PATH=... -PSIGNING_STORE_PASSWORD=... -PSIGNING_KEY_PASSWORD=...
```

`ciVersionCode` / `ciVersionName` override `versionCode` / `versionName` in
[app/build.gradle.kts](app/build.gradle.kts). Without them the build uses `1` / `"1.0"`.

## Project facts

| | |
|---|---|
| Modules | `:app` only |
| applicationId / namespace | `com.example.teamplateandroid` |
| compileSdk / minSdk / targetSdk | 37 / 28 / 36 |
| Product flavors | none — only `debug` and `release` |
| AGP / Gradle / JDK | 9.2.1 / 9.4.1 / 21 |
| Release minification | **off** (`optimization { enable = false }`) |

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
