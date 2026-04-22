# PaisaVault

**A local-first, privacy-preserving expense tracker for Android. Built entirely with free & open-source software.**

PaisaVault auto-parses your bank & UPI transaction SMS, categorizes them with on-device AI, and stores everything in an encrypted local database. No cloud. No account. No analytics. No proprietary SDKs. Your financial data never leaves your phone.

**100% FOSS** — every runtime dependency is OSI-approved or equivalently free. No Google Play Services. No ML Kit. No Firebase. Eligible for F-Droid inclusion.

---

## Why PaisaVault?

Most expense trackers either make you enter every transaction by hand or force you to hand your financial data to a cloud server. PaisaVault does neither:

- **100% local storage** — your data lives in an AES-256 encrypted SQLite database on your phone
- **Automatic tracking** — reads transaction SMS passively, no need to enter anything manually
- **On-device AI** — merchant categorization runs entirely on your phone, model never phones home
- **Zero cloud dependency** — the app works with the internet permission revoked
- **No bank app access** — only reads SMS; cannot see balances, move money, or interact with banking apps

---

## Features

### v1.0 (MVP)
- Manual expense entry with categories
- Encrypted local database
- Monthly spending dashboard with charts
- Budget limits per category
- Biometric app lock (fingerprint/face)

### v1.1 (SMS Automation)
- Historical SMS scan on first launch
- Live transaction detection from new SMS
- Regex-based parser for major Indian banks & UPI apps (HDFC, SBI, ICICI, Axis, Kotak, PhonePe, Google Pay, Paytm)
- User review queue before auto-save

### v1.2 (AI/ML)
- On-device merchant → category classifier (TensorFlow Lite)
- Anomaly detection for unusual spending
- Monthly insight summaries
- Natural language query (optional, on-device LLM)

### v1.3 (Polish)
- Receipt OCR via Tesseract4Android (Apache 2.0)
- Encrypted CSV export/import
- Encrypted local backup to user-chosen folder
- Custom categories & icons

### Future (v2.0+)
- iOS version (manual entry + receipt OCR only — iOS forbids SMS reading)
- Desktop companion (local-only sync via encrypted file)

---

## Privacy & Security

| Protection | Implementation |
|---|---|
| Data at rest | AES-256 encrypted SQLite via SQLCipher |
| Keys | Hardware-backed Android Keystore |
| App access | BiometricPrompt (fingerprint/face) + 60s auto-lock |
| Network | HTTPS-only, no analytics SDKs, INTERNET permission optional |
| Permissions | Minimal: `READ_SMS`, `RECEIVE_SMS`, `USE_BIOMETRIC` only |
| Backups | Excluded from Google Cloud Backup by default |
| Code | R8 obfuscation on release builds |
| Bank apps | **Never accessed.** SMS reading is passive and read-only. |

### Permissions rationale
- `READ_SMS` — scan historical transaction SMS on first launch
- `RECEIVE_SMS` — detect new transactions as they arrive
- `USE_BIOMETRIC` — app lock

We do **not** request: contacts, location, camera (unless you enable OCR), storage, accessibility services, or query-all-packages.

---

## Tech Stack

All runtime components are free and open-source.

| Component | Choice | License |
|---|---|---|
| Language | Kotlin | Apache 2.0 |
| UI | Jetpack Compose + Material 3 | Apache 2.0 |
| Database | Room + SQLCipher for Android (Community) | Apache 2.0 / BSD-style |
| DI | Hilt (Dagger) | Apache 2.0 |
| Async | Kotlinx Coroutines + Flow | Apache 2.0 |
| ML runtime | TensorFlow Lite | Apache 2.0 |
| OCR | Tesseract4Android | Apache 2.0 |
| On-device LLM (optional) | Phi-2 (MIT) / TinyLlama (Apache 2.0) via llama.cpp-android | MIT / Apache 2.0 |
| SMS | AOSP BroadcastReceiver + ContentResolver | Apache 2.0 |
| Background | WorkManager | Apache 2.0 |
| Auth | AndroidX BiometricPrompt | Apache 2.0 |
| Charts | Vico | Apache 2.0 |
| Testing | JUnit 4, MockK, Turbine | EPL / Apache 2.0 |
| Build | Gradle + Android Gradle Plugin | Apache 2.0 |
| JDK | Eclipse Temurin / Microsoft OpenJDK 17 | GPL + Classpath exception |
| Security scan | MobSF (dev-time only) | GPL-3.0 |

**IDE**: Android Studio is free to use and its core is Apache 2.0, but it bundles a few closed-source JetBrains components. Strict FOSS alternative: **IntelliJ IDEA Community Edition** (Apache 2.0) with the Android plugin, or pure CLI Gradle builds.

**Explicitly excluded** (to preserve FOSS status):
- Google Play Services, Firebase, Google ML Kit
- Crashlytics, Analytics, AppsFlyer, any tracking SDK
- Gemma, any non-OSI-licensed model weights

---

## Requirements

- Android 9.0 (API 28) or higher
- ~50 MB free storage
- For development: Android Studio Hedgehog (2023.1.1) or newer, JDK 17

---

## Installation (Personal Use)

The app is not published to Google Play — SMS permissions have strict Play Store review requirements. Sideload the APK onto your own device:

1. Download the latest `app-release.apk` from the project's releases
2. On your phone: Settings → Security → Allow installs from unknown sources (for your file manager)
3. Open the APK with a file manager and install
4. Grant permissions when prompted

For development, see [BUILD.md](BUILD.md).

---

## Project Structure

```
app/
├── app/                          # Android app module
│   ├── src/main/
│   │   ├── java/com/paisavault/
│   │   │   ├── data/             # Room DB, repositories, SMS parser
│   │   │   ├── domain/           # Use cases, models
│   │   │   ├── ml/               # TFLite wrapper, categorizer
│   │   │   ├── ui/               # Compose screens, ViewModels
│   │   │   ├── security/         # Keystore, biometric, encryption
│   │   │   └── di/               # Hilt modules
│   │   └── res/                  # Resources
│   └── build.gradle.kts
├── ml-training/                  # Python scripts to train TFLite model
├── BUILD.md                      # Full build & implementation plan
└── README.md
```

---

## Roadmap

See [BUILD.md](BUILD.md) for the detailed phase-by-phase plan.

- [ ] **Phase 1** — MVP with manual entry and encrypted storage
- [ ] **Phase 2** — SMS parsing and automation
- [ ] **Phase 3** — On-device ML categorization
- [ ] **Phase 4** — Insights, OCR, export/backup
- [ ] **Phase 5** — iOS companion app

---

## Contributing

This is a personal project. Issues and discussion welcome; PRs considered case-by-case.

Security disclosures: please report privately rather than filing a public issue.

---

## License

To be decided before first release. Recommended: **GPL-3.0** (forces derivatives to stay open — matches the FOSS-only ethos) or **Apache 2.0** (more permissive, includes patent grant). Avoid MIT here because it lacks the explicit patent clause.

### Third-party licenses

All dependencies and their licenses are listed in [BUILD.md § 16](BUILD.md). A `NOTICE` file with the full license text of every bundled library will ship with the release APK and be available from the Settings → About screen.

---

## Disclaimer

PaisaVault reads SMS to track expenses you initiated. It does not interact with banking apps, authenticate to banks, or handle money. You remain responsible for verifying that parsed transactions match reality. The authors make no warranty of accuracy or fitness for any purpose.
