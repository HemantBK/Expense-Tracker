# PaisaVault

> Local-first expense tracker for Android. Parses your bank and UPI SMS, categorizes with
> on-device ML, keeps everything in an encrypted database on your phone. Free and open
> source, GPL-3.0.

[![License: GPL-3.0-or-later](https://img.shields.io/badge/license-GPL--3.0--or--later-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android%209%2B-green.svg)](#requirements)
[![FOSS](https://img.shields.io/badge/FOSS-100%25-brightgreen.svg)](docs/architecture.md#foss-policy)

---

## What it does

- Reads bank and UPI transaction SMS (HDFC, SBI, ICICI, Axis, Kotak, PhonePe, Google Pay,
  Paytm) and turns them into transactions.
- Categorizes merchants on-device with a small rule + logistic-regression classifier.
- Shows monthly spend, category breakdown, and insights (month-over-month, anomaly,
  budget-limit warnings).
- Lets you set per-category budgets and alerts you at 80 % burn and again when exceeded.
- Scans paper receipts via Tesseract OCR and pre-fills the add form.
- Exports an encrypted CSV (passphrase you choose) to any folder via the Storage Access
  Framework.

All of the above runs on your device. The app declares no `INTERNET` permission.

## What it does not do

- It does not talk to your bank app or authenticate to any bank.
- It does not send any data to any server — including ours, because there is none.
- It does not include Google Play Services, Firebase, ML Kit, or any proprietary SDK.

## Permissions

- `READ_SMS`, `RECEIVE_SMS` — detect transaction messages from known bank senders
- `USE_BIOMETRIC` — optional app lock

Camera for receipt OCR uses `TakePicturePreview`, which routes through the system camera
and does not require the `CAMERA` permission.

---

## Requirements

- Android 9.0 (API 28) or newer
- ~50 MB free storage (more if you enable OCR and bundle the 2 MB Tesseract data)

## Install (personal use)

1. Download `app-release.apk` + `checksums.txt` from the latest [Release](https://github.com/HemantBK/Expense-Tracker/releases).
2. `sha256sum -c checksums.txt` — should print `app-release.apk: OK`.
3. On your phone: **Settings → Apps → Special access → Install unknown apps**, enable
   for your file manager.
4. Open the APK and install.

Not on Google Play. SMS permissions face strict Play review; sideload or wait for F-Droid.

---

## Getting started (development)

```bash
git clone https://github.com/HemantBK/Expense-Tracker.git
cd Expense-Tracker
```

Core toolchain: JDK 17, Android Studio Ladybug (2024.2.1+), Node.js 20 (for commit hooks).

First-time setup (once):

```bash
# Generate the Gradle wrapper jar if you don't have Gradle installed globally,
# Android Studio will do this automatically on first project open.
gradle wrapper --gradle-version 8.10.2 --distribution-type bin

# Install commit hooks
npm install && npx lefthook install
```

Build and run:

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Optional ML assets** — the app works without these but with reduced functionality:

```bash
# 1. Train the merchant classifier (~30 seconds)
cd ml-training
python -m venv .venv && source .venv/bin/activate   # Win: .venv\Scripts\activate
pip install -r requirements.txt && python train.py
cd ..

# 2. Download Tesseract language data for receipt OCR (~2 MB)
curl -L -o core/ml/src/main/assets/tessdata/eng.traineddata \
    https://github.com/tesseract-ocr/tessdata_fast/raw/main/eng.traineddata
```

See [`docs/repo-hygiene.md`](docs/repo-hygiene.md) for why these aren't in git.

---

## Documentation

| Doc | Purpose |
|---|---|
| [docs/architecture.md](docs/architecture.md) | Module graph, layers, data flow, tech stack |
| [docs/development.md](docs/development.md) | Dev setup, build loop, style rules |
| [docs/repo-hygiene.md](docs/repo-hygiene.md) | What's committed vs local |
| [docs/fdroid-readiness.md](docs/fdroid-readiness.md) | F-Droid submission checklist |
| [BUILD.md](BUILD.md) | Phase-by-phase build plan (source of truth for engineering work) |
| [CHANGELOG.md](CHANGELOG.md) | What shipped in each release |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Branching, commits, PR checklist |
| [SECURITY.md](SECURITY.md) | Vulnerability reporting |
| [PRIVACY.md](PRIVACY.md) | What's processed (nothing off-device) |
| [`docs/adr/`](docs/adr) | Architecture Decision Records (one file per decision) |
| [ml-training/](ml-training) | Python pipeline that trains the merchant classifier |

---

## Security posture (summary)

| Area | Guarantee |
|---|---|
| Data at rest | AES-256 via SQLCipher, key in Android Keystore |
| App access | `BiometricPrompt` with `BIOMETRIC_STRONG` + device-credential fallback |
| Task switcher | `FLAG_SECURE` blanks the preview |
| Backups | Excluded from Google cloud backup and device transfer |
| Network | No `INTERNET` permission in 1.0 |
| Exports | Passphrase-encrypted: PBKDF2 (600k) + AES-256-GCM |

Full threat model in [docs/architecture.md#security](docs/architecture.md#security).
Report a vulnerability privately via [SECURITY.md](SECURITY.md).

---

## License

**GPL-3.0-or-later.** Every source file carries an SPDX header; see [LICENSE](LICENSE).
Third-party licenses ship in the release APK and are viewable in-app at
**Settings → About → Open source licenses**.

## Disclaimer

PaisaVault reads SMS to track expenses you initiated. It does not interact with banking
apps, authenticate to banks, or handle money. You remain responsible for verifying
parsed transactions match reality.
