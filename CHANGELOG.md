# Changelog

All notable changes to this project are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] — 2026-04-23

First stable release. Feature-complete for a private, local-only expense tracker:
manual + SMS-parsed entries, on-device ML categorization, budgets with insights,
receipt OCR, and passphrase-encrypted exports. Eligible for F-Droid.

### Added — Phase 4 Insights, budgets, OCR, export
- **Budgets**: `BudgetRepository` + use cases (`SetBudgetUseCase`, `DeleteBudgetUseCase`,
  `ObserveBudgetStatusesUseCase`) + Settings → Manage Budgets CRUD UI with monthly
  spend tracking.
- **Monthly insights**: `MonthlyInsight` sealed hierarchy + `ObserveMonthlyInsightsUseCase`
  produces month-over-month comparisons, >2σ transaction anomalies, budget-near-limit
  and budget-exceeded warnings. Surfaced on the Stats screen.
- **Stats screen** now has three sections: Insights, Budgets with per-category progress,
  and by-category spend.
- **Receipt OCR** via Tesseract4Android (Apache 2.0). `ReceiptOcr` + `ReceiptParser`
  extract `(amountMinor, merchant?)` from a camera preview bitmap. Gracefully disabled
  if `eng.traineddata` asset is not bundled.
- **Add-Transaction Scan-Receipt** button — uses `TakePicturePreview` (no CAMERA
  permission needed) → OCR → auto-fills amount + merchant, then hands off to the
  existing merchant classifier for suggested category.
- **Encrypted CSV export**: `PassphraseEncryptor` implements `Encryptor` port with
  PBKDF2-HMAC-SHA256 (600k iterations) + AES-256-GCM. `ExportEncryptedCsvUseCase` wires
  CSV → encrypt → SAF-chosen location. Settings → Export data UI with passphrase
  confirmation, min-length validation, progress indicator.
- `SafExportSink` — Android-side `ExportSink` impl using ContentResolver.
- Tests: `ReceiptParserTest` (7 cases), `PassphraseEncryptorTest` (6 cases, including
  GCM tamper detection, wrong-passphrase failure, non-determinism).
- Navigation routes: `BudgetsRoute`, `ExportRoute`.

### Changed
- `app` versionName `0.1.0-alpha01` → **`1.0.0`**.

## [Unreleased]

### Added — Phase 3 On-device ML categorization
- `MerchantCategorizer` / `MerchantClassifier` abstraction with rule-first, model-second
  composition (`CompositeCategorizer`).
- `RuleCategorizer` — regex fast-path for ~60 well-known Indian merchants and UPI apps.
- `HashingVectorizer` — character n-gram vectorizer using `java.lang.String.hashCode` so
  train-side Python and serve-side Kotlin agree on feature indices bit-for-bit.
- `LogisticRegression` — multinomial softmax classifier, pure Kotlin.
- `ModelCategorizer` — loads `model_v1.json` from assets on first use, falls back silently
  when the file is absent.
- `MerchantPreprocessor` — shared normalization (strip UPI/POS/VPA noise, digit runs,
  punctuation) — Python and Kotlin implementations kept in lockstep.
- Domain port `MerchantClassifier` + use cases `SuggestCategoryUseCase`,
  `ExportTrainingDataUseCase`.
- `SmsIngestService` now calls the classifier before insert, so SMS transactions arrive
  pre-categorized. Review queue still gates unverified rows.
- Add Transaction screen — suggests a category as the user types the merchant name,
  auto-selects it while the user hasn't manually picked.
- Training pipeline `ml-training/`:
  - `train.py` — trains LR, emits `core/ml/src/main/assets/model_v1.json`.
  - `hashing.py` + `preprocessor.py` — reference implementations matching Kotlin.
  - `data/merchants.csv` — 150-entry seed dataset.
  - `MODEL_CARD.md`, `README.md`, pinned `requirements.txt`.
- Tests: `MerchantPreprocessorTest`, `RuleCategorizerTest`, `HashingVectorizerTest`.
- ADR-0007 — pure-Kotlin logistic regression over TFLite for this model.

### Added — Phase 2 SMS automation
- `SmsReceiver` (BroadcastReceiver) + `SmsParseWorker` (WorkManager) — live SMS parsing pipeline.
- `SmsHistoricalScanner` — ContentResolver-based scan of SMS inbox with progress `Flow`.
- `SmsParser` framework with pluggable `SmsParserRule` multibinding.
- Parser rules shipped: HDFC, SBI, ICICI, Axis, Kotak, PhonePe, Google Pay, Paytm.
- `SmsIngestService` — orchestrates parse → dedup → persist with synthetic reference keys.
- `SmsHistorySource` domain abstraction + `ScanSmsHistoryUseCase` keeping the feature
  layer decoupled from platform SMS APIs.
- Pending-SMS review queue: `:feature:sms-review` with confirm / reject / recategorize.
- Home screen pending banner + deep-link to review.
- Settings: permission rationale + runtime `READ_SMS` / `RECEIVE_SMS` request + scan progress.
- `WorkManager` custom `Configuration.Provider` wired in `PaisaVaultApp` for Hilt workers.
- `observePendingSms`, `observePendingSmsCount`, `markVerified` on `TransactionRepository`.
- Use cases: `ObservePendingSmsUseCase`, `ObservePendingSmsCount`,
  `ConfirmSmsTransactionUseCase`, `RejectSmsTransactionUseCase`, `ScanSmsHistoryUseCase`.
- Parser unit tests (11 cases including ReDoS and amount-overflow guards).
- ADR-0006 — rule-based-first parsing with ML categorization later.

### Added — Phase 1 MVP
- Encrypted Room database (SQLCipher) with entities for transactions, categories, budgets.
- Keystore-backed DB passphrase via `EncryptedSharedPreferences` (AES256-GCM).
- `BiometricLockManager` (BIOMETRIC_STRONG + device-credential fallback), `LockRepository`,
  `AutoLockObserver` wired to `ProcessLifecycleOwner` with 60-second background timeout.
- `LockGate` composable gates app content behind biometric auth when lock is enabled.
- `TransactionRepositoryImpl`, `CategoryRepositoryImpl` with default-category seeding.
- Domain use cases: `AddTransactionUseCase`, `ObserveMonthlyDashboard`,
  `ObserveMonthlyTransactions`, `ObserveCategoriesUseCase`, `DeleteTransactionUseCase`.
- Feature screens (full MVI contract: State + Event + Effect + ViewModel + Screen):
  `home`, `add`, `transactions`, `stats`, `settings`, `onboarding`.
- Shared UI components: `MoneyText` (locale-aware), `LoadingState`, `EmptyState`,
  `ErrorState`, `CategoryColorDot`, `CategoryChip`.
- Bottom-navigation shell with 4 tabs (Home / Transactions / Stats / Settings).
- Settings: biometric-gated app lock toggle, about section.
- Locale config declaring supported languages (en at launch).
- Unit tests: `MoneyTest`, `DateRangeTest`, `AddTransactionUseCaseTest`.
- ADR-0005 documenting MVI choice.

### Added — Phase 0 scaffold
- Multi-module Gradle project with convention plugins, version catalog,
  quality gates (ktlint, detekt, spotless), CI pipeline, repo hygiene files.
- Design system module with Material 3 theme, color / typography / spacing tokens.
- Core domain models: `Money`, `Transaction`, `Category`, `Budget`.
- `Result<T>` + `DomainError` hierarchy for exhaustive error handling.
- `MviViewModel` base class for unidirectional data flow.
- Type-safe Compose navigation with `@Serializable` routes.
- Application shell: `PaisaVaultApp`, `MainActivity` with `FLAG_SECURE`, edge-to-edge.
- ADRs 0001–0003 documenting multi-module, SQLCipher, TFLite choices.
- Repo docs: README, BUILD (MAANG-grade), CONTRIBUTING, SECURITY, PRIVACY,
  CODE_OF_CONDUCT, issue/PR templates, editorconfig, gitattributes.

### Security
- All user data stored in AES-256 encrypted SQLite via SQLCipher.
- 256-bit passphrase generated on first launch, stored in Keystore-backed prefs.
- `FLAG_SECURE` on `MainActivity` blocks screenshots and task-switcher preview.
- 60-second auto-lock on background when biometric lock enabled.
- Manifest: `allowBackup=false`, data-extraction exclusions, HTTPS-only network config,
  `hasFragileUserData=true`.
- No `INTERNET` permission in Phase 1.

<!-- Add new entries here. Sections: Added / Changed / Deprecated / Removed / Fixed / Security. -->
