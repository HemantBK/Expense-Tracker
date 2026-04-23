# Development guide

> Last reviewed: 2026-04-23 for v1.0.0.
>
> For the day-to-day loop. Architecture lives in [architecture.md](architecture.md);
> the overall plan in [BUILD.md](../BUILD.md); contribution rules in
> [CONTRIBUTING.md](../CONTRIBUTING.md).

---

## Toolchain

| Tool | Version | Install |
|---|---|---|
| JDK | 17 (Temurin or Microsoft) | `winget install Microsoft.OpenJDK.17` |
| Android Studio | Ladybug 2024.2.1+ | https://developer.android.com/studio |
| Git | 2.40+ | `winget install Git.Git` |
| Node.js | 20 LTS | `winget install OpenJS.NodeJS.LTS` |
| GitHub CLI (optional) | 2.x | `winget install GitHub.cli` |
| Python | 3.11+ (only for ML training) | https://www.python.org/ |

Verify:

```bash
java -version        # 17.x
node --version       # 20.x
./gradlew --version  # 8.10.x
```

## First-time setup

```bash
git clone https://github.com/HemantBK/Expense-Tracker.git
cd Expense-Tracker

# If the Gradle wrapper jar is absent (fresh-cloned repos without it):
gradle wrapper --gradle-version 8.10.2 --distribution-type bin
# Or: open the folder in Android Studio; it generates the wrapper for you.

# Commit hooks (one-time)
npm install
npx lefthook install

# Verify hooks
git commit --allow-empty -m "test: hook check"
```

Open in Android Studio: **File → Open → select the repo root**. Studio auto-syncs and
installs the right SDK platforms via `gradle.properties` and `build.gradle.kts`.

## Daily loop

```bash
# Fastest feedback: unit tests only
./gradlew :core:domain:test

# Full module check
./gradlew :feature:home:check

# Full workspace check (what CI runs on PRs)
./gradlew check

# Install a debug build on a connected device
./gradlew :app:installDebug

# Run a specific test
./gradlew :core:domain:test --tests '*MoneyTest*'
```

## Gradle tips

- **First build is slow** (~3–5 min for dependency resolution + KSP). Subsequent
  builds hit the configuration cache and finish in < 30 s.
- If Android Studio's sync goes weird: **File → Invalidate caches → Invalidate and
  Restart**. That's Studio's reset button; no data loss.
- `./gradlew dependencies --configuration releaseRuntimeClasspath` lists the shipped
  classpath. Useful when auditing FOSS compliance.
- To see why a dep was pulled in: `./gradlew :app:dependencyInsight --dependency hilt`.

---

## Code style

Machine-enforced. Humans don't argue formatting.

| Tool | Runs | Purpose |
|---|---|---|
| `ktlint` | pre-commit + CI | Kotlin style |
| `detekt` | CI (fails on issue) | Complexity, smells |
| `spotless` | pre-commit + CI | SPDX headers, unified format |
| Android Lint | CI | Android-specific checks |

Config lives in `config/detekt/detekt.yml` and the convention plugins in `build-logic/`.

### Project-specific rules (beyond tools)

- **No `android.util.Log`** — use Timber. `Log.*` is stripped in release builds
  anyway by our ProGuard rules.
- **No `println`** anywhere except `main()` of throwaway scripts.
- **No hardcoded UI strings** — always `stringResource(R.string.*)`.
- **Stable Compose inputs** — annotate data classes held by composables with
  `@Immutable`, use `kotlinx.collections.immutable` for list parameters.
- **SPDX header on every new `.kt`/`.kts`**:
  ```kotlin
  // SPDX-License-Identifier: GPL-3.0-or-later
  // Copyright (c) 2026 PaisaVault contributors
  ```
  Spotless adds these automatically if you forget.

### Dependency hygiene

- One source of truth: `gradle/libs.versions.toml`.
- After adding a dep: `./gradlew --write-verification-metadata sha256 --refresh-dependencies`
  and commit the updated `gradle/verification-metadata.xml`.
- **Blocked prefixes** (CI fails the build): `com.google.android.gms.*`,
  `com.google.firebase.*`, `com.google.mlkit.*`.

---

## Testing

See the pyramid and coverage targets in
[architecture.md § Testing strategy](architecture.md) (pending split) or
[BUILD.md § 27](../BUILD.md#27-testing-strategy).

| Layer | Tool | What to test |
|---|---|---|
| Domain | JUnit 4 + Kotest assertions | Use case logic, validation, math |
| Data | Fakes (preferred) + Room in-memory | Repository behavior, DAO queries |
| UI | Compose test + Robot pattern | Render + happy paths |
| Flows | Turbine | Flow emissions |
| Screenshot | Paparazzi (planned 1.1) | Design-system regression |
| Perf | Macrobenchmark (planned 1.1) | Cold start, scroll jank |

Prefer **fakes** over `MockK` for repository-boundary tests. Real fakes catch behavior
drift that mocks paper over.

### Room migration tests

Every `@Migration` needs a test in `:core:database/src/androidTest/` using
`MigrationTestHelper`. See BUILD.md § 24.3 for the pattern.

---

## Add a new screen (feature module)

1. `settings.gradle.kts` → add `":feature:foo"`.
2. Create `feature/foo/build.gradle.kts`:
   ```kotlin
   plugins { alias(libs.plugins.paisavault.android.feature) }
   android { namespace = "com.paisavault.feature.foo" }
   ```
3. Create `feature/foo/src/main/AndroidManifest.xml` with just `<manifest />`.
4. Add three files: `FooContract.kt` (State/Event/Effect), `FooViewModel.kt`
   (MVI reducer), `FooScreen.kt` (Compose + hilt ViewModel injection).
5. Add `@Serializable data object FooRoute` to `:app/src/main/kotlin/.../navigation/Routes.kt`.
6. Wire into `PaisaNavHost.kt` with `composable<FooRoute> { FooScreen() }`.
7. Add `feature/foo/src/main/res/values/strings.xml`.
8. If the new screen crosses a module boundary that's disallowed (e.g. needs data
   access), stop — write an ADR first.

## Add a new domain use case

1. New file in `core/domain/src/main/kotlin/com/paisavault/core/domain/usecase/`.
2. Constructor-inject the repository port(s) — never concrete implementations.
3. Return `Result<T>` for operations that can fail; `Flow<T>` for observables.
4. Unit test in `core/domain/src/test/kotlin/...` with a fake repository.
5. ViewModel constructor-injects the use case (not the repository).

---

## Debugging

- **Device logs**: `adb logcat | grep -i paisavault` — Timber tags log by class.
- **Room SQL**: set `Room.databaseBuilder(...).setQueryCallback { sql, args -> Timber.v(...) }`
  in `DatabaseModule` under `BuildConfig.DEBUG`.
- **Compose recomposition**: enable layout inspector in Studio, turn on
  "Highlight Recomposing Composables". If a composable recomposes more than
  expected, check for unstable params.
- **Biometric prompt not showing**: must be on `FragmentActivity`
  (`MainActivity` is). Check with `BiometricManager.canAuthenticate(...)`.
- **SMS receiver not firing in emulator**: `adb emu sms send VK-HDFCBK "Rs 500
  debited..."` sends a test SMS.
- **OCR returns null**: verify `eng.traineddata` is at
  `core/ml/src/main/assets/tessdata/eng.traineddata` and the file is not zero-length.
- **Classifier returns Uncategorized for a known brand**: `model_v1.json` is absent
  or the merchant string is outside the rule set. Run `python ml-training/train.py`.

---

## Commit & PR workflow

1. Branch: `feat/<slug>`, `fix/<slug>`, `refactor/<slug>`, `docs/<slug>`.
2. Conventional Commits, validated by `commitlint` in pre-commit.
3. Sign commits (`gpg`/`ssh`). Unsigned commits are blocked on `main`.
4. PR checklist (already in the template):
   - tests updated
   - `./gradlew check` passes
   - ADR added for non-trivial architectural changes
   - CHANGELOG entry under **Unreleased**

## Releasing

See [BUILD.md § 29](../BUILD.md#29-release-engineering) for the runbook. Key points:

- Tag format: `v1.0.0` (Semantic Versioning).
- Keystore is offline. Never commit it.
- Release workflow builds, signs, generates baseline profile, and attaches APK +
  `checksums.txt` + NOTICE to the GitHub Release.
- F-Droid metadata lives in `fastlane/metadata/android/en-US/` — update
  `changelogs/<versionCode>.txt` before every tag.

---

## Where to look when things break

| Symptom | First check |
|---|---|
| Gradle sync fails in Studio | `local.properties` SDK path, JDK 17 selected |
| `KSP task 'kspDebugKotlin' failed` | Hilt module missing `@InstallIn`, or a binding mismatch |
| App crashes on launch with SQLCipher error | `eng.traineddata` has zero length, or an old DB file from a different passphrase lives in `/data/data/.../databases/` |
| Biometric prompt dismissed immediately | `MainActivity` isn't a `FragmentActivity`, or `FLAG_SECURE` block conflicts on some OEM skins — we accept this |
| FOSS CI job fails | A new dep pulled in `gms`/`firebase`/`mlkit` transitively — find the culprit via `./gradlew dependencies` |
| Release APK installs fail with `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Debug and release apps have different `applicationId` suffixes (`.debug` vs not), can coexist; use `adb uninstall com.paisavault.debug` if needed |
