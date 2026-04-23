# Contributing to PaisaVault

Thanks for your interest. Everything here is designed to keep the codebase healthy and the project's privacy guarantees intact.

Before a non-trivial change, **open an issue or discussion first**. Small fixes can go straight to PR.

---

## Development environment

See [BUILD.md § 2](BUILD.md#2-prerequisites--setup) for the full list. Minimum:

- JDK 17 (Eclipse Temurin or Microsoft OpenJDK)
- Android Studio Ladybug+ or IntelliJ IDEA Community + Android plugin
- Git 2.40+
- Node.js 20+ (for commit hooks)

Clone and bootstrap:

```bash
git clone https://github.com/HemantBK/Expense-Tracker.git
cd Expense-Tracker
./gradlew --version       # triggers wrapper download
npx lefthook install      # installs pre-commit hooks
```

---

## Development loop

```bash
# Install debug build on connected device
./gradlew :app:installDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run Compose UI tests
./gradlew connectedDebugAndroidTest

# Lint + format check
./gradlew ktlintCheck detekt spotlessCheck

# Auto-format
./gradlew spotlessApply ktlintFormat

# Full check (what CI runs)
./gradlew check
```

---

## Branch & commit conventions

- **Branches**: `feat/<short-slug>`, `fix/<short-slug>`, `refactor/<short-slug>`, `docs/<short-slug>`
- Never commit directly to `main`
- **Conventional Commits** (enforced by `commitlint`):

```
feat(feature:home): show monthly total on dashboard
fix(data:sms): parse HDFC debit SMS with multi-line body
refactor(core:domain): extract Money value class
docs(adr): add ADR 0005 for MVI choice
test(data:sms): cover ICICI credit regex edge cases
perf(app): generate baseline profile
chore(deps): bump AGP to 8.7.1
```

Breaking changes: `feat!: <description>` or include `BREAKING CHANGE: <description>` in body.

Sign your commits (`gpg` or `ssh`). Unsigned commits blocked on `main`.

---

## Pull requests

Your PR must:

- [ ] Target `main`
- [ ] Link an issue (or explain why one isn't needed)
- [ ] Pass all CI jobs (ktlint, detekt, unit + instrumented tests, lint, FOSS check, APK size)
- [ ] Include tests for new behavior; fix = regression test
- [ ] Add/update ADR if the change is architectural ([`docs/adr/`](docs/adr))
- [ ] Add/update TDD if the change is a non-trivial new feature ([`docs/tdd/`](docs/tdd))
- [ ] Update `CHANGELOG.md` under the Unreleased section (auto-generated at release, but curate)
- [ ] Update documentation affected (README, BUILD, PRIVACY, etc.)
- [ ] Keep the PR focused — one logical change per PR

PR body should state **what** and **why**, not how. The diff says how.

---

## Code style

Style is enforced by **ktlint + Spotless**. Formatting questions: let the tool decide.

Key principles:

- **No hardcoded strings in UI** — use `R.string`
- **No `android.util.Log`** — use `Timber`
- **No `println`** in production code
- **No `!!`** unless you can justify it in a comment
- **Prefer fakes over mocks** for repository-boundary tests
- **Stable types in Compose** — annotate `@Immutable` / `@Stable` as needed; use `kotlinx.collections.immutable` for list params
- **Forbidden imports**: `com.google.android.gms.*`, `com.google.firebase.*`, `com.google.mlkit.*` (CI blocks)
- **SPDX header** on every new `.kt`/`.kts` file:
  ```kotlin
  // SPDX-License-Identifier: GPL-3.0-or-later
  // Copyright (c) 2026 PaisaVault contributors
  ```

---

## Testing expectations

| Layer | Target coverage | Test style |
|---|---|---|
| `:core:domain` | 90% | Unit (pure Kotlin) |
| `:core:common` | 90% | Unit |
| `:data:*` | 80% | Integration with fakes; real Room in-memory for DAO tests |
| `:feature:*` | 60% | Compose UI tests (Robot pattern), screenshot tests |

- Use `Turbine` for Flow assertions
- Use `Kotest property` for parser fuzz tests
- Use `Paparazzi` for screenshot tests on design-system components
- Use `Macrobenchmark` for performance regressions

---

## Architecture boundaries

The module graph IS the architecture. The wrong dependency won't compile:

| From | Allowed |
|---|---|
| `:app` | Everything |
| `:feature:*` | `:core:domain`, `:core:common`, `:core:ui`, `:core:design-system` |
| `:data:*` | `:core:database`, `:core:datastore`, `:core:domain`, `:core:common`, `:core:security` |
| `:core:domain` | Kotlin stdlib only |
| `:core:common` | Kotlin stdlib (ideally) |

Don't add a dependency to break out of these. Propose the right module in an ADR first.

---

## Adding a dependency

1. Check it's **OSI-approved** (or equivalently free, documented).
2. Add to `gradle/libs.versions.toml` — nowhere else.
3. Run `./gradlew --write-verification-metadata sha256` and commit the updated `gradle/verification-metadata.xml`.
4. In your PR body, state why this dependency is needed and what alternatives you considered.
5. If the dependency is > 500 KB compiled, justify it against the APK size budget.

**Blocked groups**: `com.google.android.gms.*`, `com.google.firebase.*`, `com.google.mlkit.*`. CI will reject.

---

## Accessibility checklist (every UI PR)

- [ ] Interactive elements have `contentDescription` or semantics role
- [ ] Touch target ≥ 48dp (`Modifier.minimumInteractiveComponentSize()`)
- [ ] Tested with TalkBack on emulator or device
- [ ] Works at 200% font scale without overlap
- [ ] Contrast passes WCAG AA (use Accessibility Scanner)
- [ ] RTL-safe (test by switching locale to Arabic)

---

## Security-sensitive changes

Changes touching `:core:security`, `:core:database`, SMS parser, or crypto require:

- An ADR describing the change
- A reviewer with security context
- No shortcuts around Keystore
- No logging of sensitive values
- `adb backup` extraction test re-run before merge

Report vulnerabilities privately per [SECURITY.md](SECURITY.md).

---

## Releasing

Maintainers only. See [BUILD.md § 29](BUILD.md#29-release-engineering) for the full runbook.

---

## Questions?

Open a [Discussion](https://github.com/HemantBK/Expense-Tracker/discussions). For private matters, see SECURITY.md contact.
