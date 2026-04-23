# F-Droid readiness — v1.0.0

Checklist for submitting PaisaVault to fdroiddata.

## Inclusion criteria checks

- [x] **Fully FOSS**: no proprietary dependencies (`com.google.firebase.*`,
      `com.google.android.gms.*`, `com.google.mlkit.*` all blocked — see
      [BUILD.md § 33](../BUILD.md#33-licensing--foss-compliance)).
- [x] **Source available**: public repo at https://github.com/HemantBK/Expense-Tracker
- [x] **License**: GPL-3.0-or-later (declared in `LICENSE`; SPDX headers on every file).
- [x] **Reproducible build**: Gradle configuration cache enabled, dependency verification
      with committed checksums in `gradle/verification-metadata.xml`.
- [x] **No anti-features**: no tracking, no ads, no proprietary network services, no
      non-free-net calls in default config.
- [x] **Signed tags**: release tags are `gpg --sign`-able.

## Anti-feature notes (to declare in metadata)

- `NonFreeNet`: **None** — app has no `INTERNET` permission in v1.0.
- `NonFreeAdd`: **None**.
- `Tracking`: **None**.
- `NonFreeDep`: **None**.
- `NonFreeAssets`: potentially the Tesseract `eng.traineddata` (Apache 2.0 — still FOSS
  but a large binary that ideally is built from source). For strict purity, F-Droid
  reviewers may ask to rebuild it; we can generate it from `tessdata_best` training
  corpus if required.
- `UpstreamNonFree`: **None**.

## fdroiddata metadata checklist

Files in `fastlane/metadata/android/en-US/` are already set up:

- [x] `title.txt`
- [x] `short_description.txt`
- [x] `full_description.txt`
- [x] `changelogs/1.txt` (update for each release)
- [ ] **Screenshots** (add before submission): `phoneScreenshots/*.png`
- [ ] **Icon** `images/icon.png` (512×512 PNG)
- [ ] **Feature graphic** `images/featureGraphic.png` (1024×500)

## Build config for fdroiddata YAML

Example `metadata/com.paisavault.yml`:

```yaml
Categories:
  - Money
License: GPL-3.0-or-later
AuthorName: HemantBK
SourceCode: https://github.com/HemantBK/Expense-Tracker
IssueTracker: https://github.com/HemantBK/Expense-Tracker/issues

AutoName: PaisaVault
Summary: Local-first, privacy-preserving expense tracker

RepoType: git
Repo: https://github.com/HemantBK/Expense-Tracker.git

Builds:
  - versionName: 1.0.0
    versionCode: 1
    commit: v1.0.0
    subdir: app
    gradle:
      - yes
    rm:
      - core/ml/src/main/assets/model_v1.json  # regenerated at build; see below
    prebuild: |
      cd ml-training
      pip install -r requirements.txt
      python train.py --out ../core/ml/src/main/assets/model_v1.json
    # If eng.traineddata must be bundled, add:
    # - rm: core/ml/src/main/assets/tessdata/eng.traineddata (if bundled)
    # - prebuild: curl -L -o ... (F-Droid will reject this; ship from source only)

AutoUpdateMode: Version v%v
UpdateCheckMode: Tags
CurrentVersion: 1.0.0
CurrentVersionCode: 1
```

## Steps before submission

1. Create an initial tagged release `v1.0.0`.
2. Generate screenshots on a physical device or emulator (Pixel 6a at 1080×2400):
   - Home, Stats, Transactions, Add, SMS Review, Budgets, Settings
   - Dark + light variants
3. Produce the 512×512 PNG icon from `ic_launcher_foreground.xml` at higher resolution.
4. Update `fastlane/metadata/android/en-US/changelogs/1.txt`.
5. Fork https://gitlab.com/fdroid/fdroiddata, add metadata/com.paisavault.yml, open MR.
6. Follow up on reviewer comments (typically asks about non-free assets or embedded
   binaries).

## What might block submission

- **Tesseract traineddata** as shipped binary — may need to either (a) exclude OCR from
  F-Droid flavor or (b) build the file from source in the prebuild step (complex).
- **`model_v1.json`** — if committed, reviewers may flag as "not reproducible from
  source". Solution: don't commit it; let F-Droid build it from `train.py` in prebuild.
- **Gradle wrapper JAR** — committed binary. F-Droid uses its own copy, so this is OK.

## Post-submission maintenance

- Every release: tag, update fastlane changelog, F-Droid's tag watcher picks it up.
- Monitor https://monitor.f-droid.org for build failures after merges.
