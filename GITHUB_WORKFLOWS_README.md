# GitHub Workflows Implementation Guide

A guide to the GitHub workflow system in this project: parallel release branch
management, PR-comment-driven tagging, and tag-dispatched CI.

For secrets/variables setup see [CI_CD_SETUP.md](CI_CD_SETUP.md). For branch
strategy and diagrams see [github_workflow.md](github_workflow.md).

## 🚀 Quick Start

### 1. Create a New Release

**Method 1: GitHub Issue (Recommended)**

1. Create a new GitHub issue with the release version as the title (e.g., `1.0.0`)
2. In the description, optionally specify the base commit:
   ```markdown
   Base commit: abc1234

   Release planning details, feature list, timeline...
   ```
3. The system automatically creates `release/1.0.0` branch and tracking PR

**Method 2: Manual Workflow**

1. Go to Actions → Release Branch Creator → Run workflow
2. Enter version (e.g., `1.0.0`) and optional commit hash
3. The system creates the release branch and tracking PR

### 2. Develop Features

1. Create feature branches targeting the release branch:
   ```bash
   git checkout release/1.0.0 && git checkout -b feature/user-authentication
   ```

2. Create PR: `feature/user-authentication` → `release/1.0.0`

3. On merge, `release-drafter.yml` refreshes the draft release notes and rewrites
   the tracking PR body.

### 3. Create Release Candidates

In the release tracking PR, comment:

- `/build rc` → Creates `1.0.0-rc01`, `1.0.0-rc02`, etc.

### 4. Create Production Release

When QA approved, comment in the release tracking PR:

- `/build release` → Creates the `1.0.0` production tag

### 5. Complete Release

1. The production release CI must finish successfully.
2. Release assets are uploaded to the GitHub Release.
3. The release PR (`release/1.0.0` → `main`) is auto-merged after successful production CI.
4. Manually update other active release branches with main changes.

## 📋 Workflow Files Overview

### Core Workflows

| Workflow | Purpose | Trigger |
|----------|---------|---------|
| `release-branch-creator.yml` | Creates release branches and tracking PRs | Issues, manual dispatch |
| `release-drafter.yml` | Maintains draft release notes and the tracking PR body | Push to `release/*` |
| `tag-on-pr-comment.yml` | Parses `/build` and `/tag` PR comments, creates annotated tags | PR comments |
| `release-tag.yml` | Creates GitHub Releases from tags, dispatches CI, uploads assets, auto-merges | Tag push, CI completion |
| `ci.yml` | Parameterized build/distribution pipeline (`version_name`, `git_ref`) | Manual dispatch only |

### Reusable Workflows (called by `ci.yml`)

| Workflow | Purpose |
|----------|---------|
| `job-build.yml` | Unit tests, then signed `assembleRelease` + `bundleRelease` |
| `job-firebase.yml` | Firebase App Distribution upload, collects share links |
| `job-drive.yml` | Google Drive upload |

### Supporting Workflows

| Workflow | Purpose |
|----------|---------|
| `cache-warmer.yml` | Warms the Gradle/Android cache on every push to `main` |
| `remove-old-artifacts.yml` | Deletes build artifacts older than 1 day (keeps 5 most recent) |
| `sync-template.yml` | Daily PR with changes pulled from the upstream template repo |

## 🏗️ System Architecture

### Git Flow Strategy

```
main (protected)
├── release/1.0.0 (protected)
│   ├── feature/login
│   └── feature/auth
├── release/1.1.0 (protected)
│   ├── feature/dashboard
│   └── feature/settings
└── feature/general (unrestricted)
```

**Branch Rules:**

- `main`: Direct push/commit prohibited (PR only)
- `release/X.Y.Z`: Direct push/commit prohibited (PR only)
- Other branches: No restrictions

Note: these rules are a **convention you must configure** in repository branch
protection settings. No workflow enforces them.

### Tag-Driven Release Execution

1. `tag-on-pr-comment.yml` validates the PR comment command and creates an annotated
   release tag carrying `Release-Metadata:` trailers.
2. `release-tag.yml` reacts to the tag push, creates/updates the GitHub Release, sends
   the Discord start notification, and dispatches `ci.yml` asynchronously.
3. `ci.yml` builds from explicit inputs:
   - `version_name`: used as the app version name and artifact suffix.
   - `git_ref`: commit hash, tag ref, or branch to check out and build.
   - optional `version_code`: overrides the default run-number-based version code.
4. `ci.yml` uploads build/Firebase/Drive artifacts plus a metadata artifact.
5. `release-tag.yml` reacts to `ci.yml` completion, uploads APK/AAB/mapping assets to
   the GitHub Release, sends the final Discord notification, and auto-merges production
   release PRs after successful CI.

Branch authorization lives in `tag-on-pr-comment.yml`. `ci.yml` intentionally does not
re-validate branch or tag origin — it builds whatever `git_ref` it is handed.

### Parallel Release Development

```mermaid
graph TB
    A[Create Issue: "1.0.0"] --> B[release/1.0.0 branch]
    C[Create Issue: "1.1.0"] --> D[release/1.1.0 branch]

    B --> E[Feature A → release/1.0.0]
    B --> F[Feature B → release/1.0.0]

    D --> G[Feature C → release/1.1.0]
    D --> H[Feature D → release/1.1.0]

    E --> I[RC: 1.0.0-rc01]
    I --> J[Production: 1.0.0]

    G --> K[RC: 1.1.0-rc01]
    K --> L[Production: 1.1.0]
```

## 🛠️ Commands Reference

### Release Branch Creation

**Via GitHub Issue:** title `1.0.0`, description `Base commit: abc1234` (optional).

**Via Workflow Dispatch:** version `1.0.0`, commit hash `abc1234` or `main`.

### Tag Creation Commands

Use these commands in PR comments. The commenter must have `write`, `maintain` or
`admin` permission.

| Command | Effect | Allowed source branches |
|---------|--------|-------------------------|
| `/build rc` | Creates next RC (`1.0.0-rc01`, `rc02`...) | `release/X.Y.Z` only |
| `/build release` | Creates production tag (`1.0.0`) | `release/X.Y.Z` only |
| `/tag 1.0.0-beta1` | Creates a specific tag | `main`, `release/X.Y.Z` |

Validation is against the PR's **source (head) branch**, not the base branch.

Re-running `/build release` for an existing production tag **deletes and recreates**
the tag at the newest commit. RC tags are never recreated — the command fails if the
tag already exists.

### Branch Protection Setup

Configure manually in repository settings — no workflow does this:

- `main`: require PR reviews and status checks
- `release/**`: require PRs

## 🚦 CI/CD Pipeline Flow

### Release Candidate Workflow

1. **Trigger:** `/build rc` creates an RC tag from the release tracking PR.
2. **Orchestration:** `release-tag.yml` creates a GitHub prerelease and dispatches `ci.yml`.
3. **Actions:**
   - Unit tests, then build and sign artifacts in `job-build.yml`
   - Upload to Firebase and Google Drive when the relevant secrets are configured
   - Upload APK/AAB assets to the GitHub prerelease after CI succeeds
   - Discord start/final notifications from `release-tag.yml`

### Production Release Workflow

Same as above, plus: the source release PR is auto-merged, but **only** when the tag is
hyphen-free, CI succeeded, and asset upload succeeded.

## 📊 Distribution Channels

Each channel is skipped automatically when its secrets are missing:

| Channel | Requires | Skipped when absent |
|---|---|---|
| GitHub Releases | — | never |
| Firebase App Distribution | `FIREBASE_DISTRIBUTION_CREDENTIAL_JSON` | ✅ |
| Google Drive (APK + AAB) | `GG_DRIVE_CLIENT_ID` + `GG_DRIVE_CLIENT_SECRET` + `GG_DRIVE_REFRESH_TOKEN` + `GG_DRIVE_FOLDER_ID` | ✅ |
| Discord | `DISCORD_WEBHOOK` | ✅ |

## 🔍 Monitoring and Validation

### Branch Validation

- All tag commands: source branch must be `main` or `release/X.Y.Z`
- `/build rc` and `/build release`: additionally rejected on `main`
- Unauthorized branches: the workflow comments on the PR and stops

### Error Handling

- Permission validation before any tag work
- Failures are reported back as PR comments and Discord notifications
- `release-tag.yml` fails the job explicitly when CI dispatch or finalization fails

## 🎯 Best Practices

### Release Planning

1. Create a GitHub issue for release planning
2. Use the issue description for base commit specification
3. Link related issues to the release milestone
4. Track features in the release PR checklist

### Feature Development

1. Target the appropriate branch (`main` for general, `release/X.Y.Z` for release-specific)
2. Keep feature branches focused and small
3. Label PRs so release-drafter categorises them (`feature`, `fix`, `docs`, `dependencies`)
4. Use descriptive commit messages

### Release Process

1. Create multiple RC builds for thorough testing
2. Validate all features before production release
3. Merge release branches back to main promptly
4. Update other active release branches after the main merge

## 🚨 Troubleshooting

**Tag creation fails:**

- Check commenter permissions (write access required)
- Verify the PR *source* branch is `main` or `release/X.Y.Z`
- For `/build rc`/`/build release`, the source branch cannot be `main`
- Ensure an RC tag with that name doesn't already exist
- Verify `GH_PAT` is set and unexpired

**Tag created but nothing else happens:**

The tag was pushed with `GITHUB_TOKEN` instead of the PAT. Tags pushed with the
built-in token do not trigger other workflows. Check the PAT secret.

**CI dispatch or finalization fails:**

- `release-tag.yml` needs `actions: write` to dispatch `ci.yml`
- `ci.yml` must exist on the repository default branch
- Confirm the CI run uploaded `ci-release-metadata-*` and build artifacts
- The dispatch must use the PAT — `GITHUB_TOKEN`-dispatched runs emit no `workflow_run` event

**Release notes not appearing in the tracking PR:**

- The PR must carry one of the labels `release`, `tracking`, `release-drafter`
- The PR body must still contain the literal `## What's Changed` and
  `## 🔧 Release Information` markers — `release-drafter.yml` replaces the text between them

**Build failures:**

- Check all four `SIGNING_*` secrets are set
- Verify the keystore base64 decodes cleanly (`base64 -w0`, no line wrapping)
- Review dependency conflicts in the job log

**Template sync does nothing:**

- The `TEMPLATE_SOURCE_REPO` variable is unset — the job skips itself by design
- `TEMPLATE_SYNC_TOKEN` must be able to *read* the template repo and *push* to this one
- A run that reports "no changes" is normal; the push step is `continue-on-error`

**Template sync overwrote my app code:**

Check `.templatesyncignore`. Anything not listed there is replaced wholesale on every
sync. Add the path, then revert the offending sync PR.

**Distribution issues:**

- Firebase: verify the service account has the *Firebase App Distribution Admin* role
- Firebase: an app id is needed — commit `app/google-services.json`, set the
  `GOOGLE_SERVICES_JSON` secret, or set the `FIREBASE_APP_ID` variable
- Drive: both Drive secrets must be present or the job is skipped entirely
- Check the tester group name in `.distribution/tester_groups.txt` exists in Firebase

---

This workflow system enables Android release management with automation, quality
controls, and team coordination.
