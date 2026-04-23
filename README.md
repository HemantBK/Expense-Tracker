# Expense Tracker

> Local-first expense tracker for Android. Parses your bank and UPI SMS, categorizes with
> on-device ML, keeps everything in an encrypted database on your phone. Free and open
> source, GPL-3.0.
>
> **Working name**: "Expense Tracker". Internal Gradle `applicationId` and package paths
> (`com.paisavault.*`) are unchanged for now; they'll be renamed in one pass once the
> final product name is decided.

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

---

## How this app is different from typical expense trackers

Most expense-tracking apps follow the same shape: a company runs a cloud service, you
give them your bank login or a middleware token, they pull your transactions, and you
pay them with a subscription, with your attention (ads), or with your data. The
differences between those apps are mostly cosmetic — the dashboard looks sharper, the
categorization is smarter, the notifications are cleverer. The trust model is the same
underneath.

This app is built on a different model entirely.

### Ownership & control

| Dimension | Typical cloud tracker | This app |
|---|---|---|
| Where your data lives | A company's database, in their cloud region | One encrypted file on your phone |
| Who can read the raw data | Their engineers, DB admins, analytics pipeline, and anyone with a subpoena against the company | Only you, behind your biometric |
| What you get if you leave | An export after going through a settings flow | You already have it — it's already yours |
| What you get if the project ends | Typically: a data-download window, then gone | Source, data, and your installed APK — unchanged, forever |
| Who can change the terms of service | They can, at any time | There are no terms |
| Who decides what features exist | A product team with a roadmap | You (fork) or the maintainers (in the open) |

### Business model & incentives

| Dimension | Typical cloud tracker | This app |
|---|---|---|
| How maintainers make money | Subscription, freemium upsell, ads, or reselling anonymized transaction data | They don't — GPL-3.0, no commercial model |
| What gets measured about you | Taps, sessions, merchants, categories — often resold | Nothing is measured |
| Incentive when a feature conflicts with privacy | Growth usually wins | Privacy was the reason the app exists |
| Pressure to add features that send data out | High — the pipeline already exists | Zero — there's no pipeline to send to |
| "Offer" and partner-product notifications | Common | None — no server to send them from |

### Transparency

| Dimension | Typical cloud tracker | This app |
|---|---|---|
| What the binary actually does | You trust the privacy policy | You (or anyone) can read the source |
| Third-party SDKs in the APK | Often 15–30+, rarely enumerated | Zero proprietary SDKs; CI fails the build if a blocked SDK is transitively pulled in |
| Security claims | A policy page and a compliance badge | Every dependency is SHA-256 pinned, threat model is a file in the repo, every significant decision is an ADR |
| Can you audit before installing | Only by reputation | `apkanalyzer`, `apksigner`, MobSF, and the source itself — all FOSS |
| "Anonymized" data claims | Often re-identifiable in practice | No data is collected, anonymized or otherwise |

### What breaks differently when something goes wrong

Something always goes wrong eventually. The question is what the blast radius looks like.

| Scenario | Typical cloud tracker | This app |
|---|---|---|
| One user's credentials phished | That user's bank + their full history at risk | No credentials exist — unaffected |
| The operator has a data breach | All users exposed at once | There is no operator and no shared datastore |
| The operator is acquired; new owner revises the privacy policy | You accept or extract and leave | Nothing changes — the code you compiled is the code you run |
| The operator shuts down | Data goes away on the announced date | App keeps working; data is still on your phone |
| A subpoena targets your records | The operator must comply | No operator exists to subpoena; a request must come to you directly |
| You lose your phone | Usually recoverable from the cloud account | Data is gone with the phone |

That last row is a real tradeoff, not a win. You pick your threat model: lose-the-phone
risk versus cloud-breach risk. This app optimizes for the second.

### Features we chose to do differently

- **Auto-parse SMS, but never auto-trust.** Parsed transactions land in a review queue
  marked unverified. You confirm, recategorize, or reject. Dashboards count only what
  you've confirmed. The common cloud pattern posts parsed transactions straight into
  the feed because their parsing is server-side and corrections are round-trips.
- **Learn from your corrections, on your phone.** The merchant classifier can be
  retrained against your own verified history. The training script runs on your
  laptop, not a shared ML cluster. Your labels never train a model that anyone else
  uses.
- **Receipt OCR without the camera permission.** Scanning routes through the system
  camera activity (`TakePicturePreview`); we never hold direct camera access, just the
  returned bitmap. Fewer permissions means less attack surface.
- **Budget warnings that stay on-device.** There's no push server. Alerts are driven
  by the local database, not pushed from a backend. No one else learns that you went
  over on Food this week.
- **Exports you encrypt, with a passphrase only you hold.** The `.pvxc` file is
  AES-256-GCM over a PBKDF2-derived key (600,000 iterations). Even if the file ends up
  in a cloud drive or email, it stays sealed.
- **Training data stays yours.** When you correct a classifier mistake, that
  `(merchant, category)` pair is saved only in your encrypted database. It is not
  uploaded, not federated, not mixed with anyone else's labels.

### Regulatory angle

Because there is no server, no account, and no data leaves the device by default, the
app falls under the **personal / domestic processing exemption** of India's DPDP Act
(§ 3(c)(ii)). You are your own data principal; there is no data fiduciary to register
or hold accountable — there's no processing happening that the Act was written to
govern. Equivalent reasoning applies to GDPR and CCPA: the frameworks governing
cloud-data processing don't activate when no cloud processing occurs.

### When this app is NOT the right fit

Honest list:

- You want multi-device sync without running any infrastructure of your own.
- You share a household budget with a partner and want a live shared view.
- You're on iOS — iOS forbids third-party SMS reading, so this exact design doesn't
  port. A manual-entry + receipt-OCR version is possible but loses the headline
  feature.
- You want AI-generated natural-language insights across your whole history — an
  on-device small model cannot match what a large server-side model can produce.
- You want the app to *do* something with your data: negotiate a subscription
  cancellation, dispute a charge, apply for credit. Those actions require an operator
  that holds credentials on your behalf.

Anything on that list is a different product. This one is for people who want the
numbers tracked and the data staying put.

---

## How we keep your data safe

The typical expense-tracker model works like this: the app asks for your bank login,
stores it on a server, scrapes your transactions nightly, and shows you a dashboard.
That model has four places something can go wrong — your credentials, the operator's
servers, their employees, and the analytics / ad pipelines they feed. A single mistake
in any of those can expose every user at once.

This app removes that entire model.

### Structural choices (the big risks are gone by design)

| Choice we made | Attack class it eliminates |
|---|---|
| No server, no backend, no cloud account | Remote server breach. There is nothing to breach. |
| No user account, no signup, no password | Credential stuffing, phishing, reset-flow hijack, password reuse — none of these exist when there's no account. |
| We never ask for, hold, or transmit bank credentials | OAuth-token theft, aggregator credential-store compromise. |
| Only passive SMS reading; no interaction with any banking app | Screen-scraping abuse, accessibility-service malware patterns. |
| No analytics, no ads, no tracking, no third-party SDKs | Data pipelines that could exfiltrate transactions or metadata. |
| No `INTERNET` permission declared in this release | The Android OS itself blocks all outbound traffic regardless of what the app code does. |
| No cloud sync — exports are explicit, user-initiated, and encrypted | Silent background sync that could fail open; MITM on sync traffic; leaked backups. |

Each row is a class of attack that targets a shape we simply don't have.

### Technical protections (what protects the data that IS on your phone)

| Protection | How it's implemented |
|---|---|
| Database encryption at rest | AES-256 page-level encryption via SQLCipher across every table |
| Encryption key | 256-bit random, generated on first launch, stored in Keystore-backed `EncryptedSharedPreferences` (hardware-backed on devices with StrongBox) |
| App unlock | `BiometricPrompt` at `BIOMETRIC_STRONG` only — weak face-unlock is rejected — with device-credential (PIN / pattern / password) fallback |
| Auto-lock | 60 seconds after backgrounding, wired into the process-level lifecycle observer |
| Screenshot + task-switcher privacy | `FLAG_SECURE` on every activity; the preview in recent-apps is blanked; screen recordings refuse to capture |
| Backup exclusion | `allowBackup=false` plus data-extraction rules that block Google cloud backup and device-to-device transfer |
| Export file encryption | PBKDF2-HMAC-SHA256 with 600,000 iterations to derive a key from your passphrase, then AES-256-GCM with authenticated encryption. Wrong passphrase = no oracle leak; tampered file = GCM auth fails |
| SMS input hardening | 2 KB body cap, 10 crore amount cap, 80-char merchant cap, ReDoS-safe regex |
| Build hardening | R8 full-mode minification + resource shrinking; APK Signature Scheme v3; release keystore kept offline |
| Supply chain | Gradle dependency verification pins SHA-256 of every bundled library; a CI job fails the build if any proprietary Google SDK appears in the release classpath |
| OS isolation | Standard Android UID sandbox; no exported `ContentProvider`, `Service`, or `Activity`; the SMS receiver is protected by `android.permission.BROADCAST_SMS` so only the OS can invoke it |

### What you get that you can't get from a cloud-connected tracker

- **The scope of a worst-case breach is one phone.** Even if an attacker compromised
  this app on one device, no other user is affected. There is no shared datastore.
- **We can't sell your data** — we don't have it, so there's nothing to monetize. The
  app is GPL-3.0, not ad-supported and not subscription-funded.
- **We can't change the deal on you.** There's no terms-of-service update that could
  start harvesting your data tomorrow — the app has no code path to send data out.
- **You can audit it.** Every runtime dependency is OSI-approved and pinned. Every
  decision with security impact has an ADR in [`docs/adr/`](docs/adr).

### What we can't protect against — the honest list

No security posture is total. Here's what remains on you or on us in ways we can't
eliminate:

- **Physical access to your unlocked phone.** Biometric + auto-lock raise the cost;
  nothing makes it zero. If someone else can open your phone, they can open the app.
- **A rooted or compromised operating system with an active attacker.** Hardware-backed
  keys raise the bar, but kernel-level compromise beats any app-level defense. We
  deliberately don't implement root detection — it's easily bypassed and gives false
  confidence.
- **A supply-chain compromise we haven't caught.** Dependency verification catches a
  swapped artifact at build time; it does not catch a malicious release from an
  upstream project you then pin. We keep the dependency list small and boring for
  exactly this reason.
- **A bad passphrase on your encrypted export.** We enforce a minimum length; we can't
  enforce that you don't use `password123`. A weak passphrase on a `.pvxc` file you
  share is a weak passphrase.
- **A compromised release signing key.** If our signing keystore is stolen and used to
  sign a tampered APK, the update mechanism can't tell. Mitigation: keystore kept
  offline; every release's SHA-256 is published; if anything looks off, verify before
  installing.

### Further reading

- Full threat model: [`docs/architecture.md#security`](docs/architecture.md#security)
- Report a vulnerability privately: [`SECURITY.md`](SECURITY.md)
- What is (and isn't) processed: [`PRIVACY.md`](PRIVACY.md)
- Why we chose SQLCipher: [`docs/adr/0002-use-sqlcipher-for-local-encryption.md`](docs/adr/0002-use-sqlcipher-for-local-encryption.md)
- Export crypto choice: [`docs/adr/0008-encrypted-export-format.md`](docs/adr/0008-encrypted-export-format.md)

---

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

## License

**GPL-3.0-or-later.** Every source file carries an SPDX header; see [LICENSE](LICENSE).
Third-party licenses ship in the release APK and are viewable in-app at
**Settings → About → Open source licenses**.

## Disclaimer

This app reads SMS to track expenses you initiated. It does not interact with banking
apps, authenticate to banks, or handle money. You remain responsible for verifying
parsed transactions match reality.
