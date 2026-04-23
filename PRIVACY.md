# Privacy Policy

**Effective date:** *(to be set on first release)*

PaisaVault is built on a single principle: **your financial data never leaves your device.** This document describes what that means in practice, what data is processed, and what promises we are making.

---

## Plain-English summary

- We collect **nothing.** Not even crash reports, not even anonymous analytics.
- All data stays on your phone, encrypted at rest.
- The app functions with no internet permission granted.
- You can uninstall and all data is gone.

---

## What PaisaVault processes

On your device only:

| Data | Purpose | Where stored |
|---|---|---|
| Transaction SMS (from bank/UPI senders) | Parse amount, merchant, date | Encrypted SQLite DB on device |
| Parsed transaction records | Show expenses, totals, charts | Same DB |
| Categories and budgets | Classify and limit spending | Same DB |
| Biometric authentication result | Unlock the app | Android Keystore — never stored; only verified |
| User-provided labels (category corrections) | Improve on-device ML model | Same DB |
| App settings | Theme, biometric toggle, feature flags | Encrypted DataStore |
| Crash reports (if enabled) | Local debugging only | Encrypted local file; never uploaded |

PaisaVault does **not** process:

- Contacts, call logs, location, camera (unless you use receipt OCR, which processes image locally then discards)
- Non-transaction SMS (personal messages are ignored by the parser)
- Banking credentials, OTPs, card numbers (the app never asks for any)
- Any identifier that could link your data to you

---

## What PaisaVault transmits

**Nothing, by default.**

In v1.0, the `INTERNET` permission is not requested. The app literally cannot make network calls.

If a future version adds optional internet use (e.g., to download a larger ML model at user request, or check for app updates), it will:

- Require explicit user opt-in
- Be documented in this policy before release
- Use HTTPS with certificate pinning
- Only contact resources listed here

---

## Permissions explained

| Permission | Why |
|---|---|
| `READ_SMS` | Scan historical SMS from known bank/UPI senders on first launch |
| `RECEIVE_SMS` | Detect new transaction SMS as they arrive |
| `USE_BIOMETRIC` | Biometric app lock |
| (Optional) `CAMERA` | Receipt OCR — image processed locally and discarded |

You may revoke any permission at any time in Android Settings. The app degrades gracefully — without SMS access, it becomes a manual-entry tracker.

The app does **not** request:

- Contacts, location, storage, accessibility services
- Phone, microphone, call logs
- `QUERY_ALL_PACKAGES` (cannot see your other apps)

---

## Third parties

**None.**

No analytics SDK. No crash reporter. No advertising. No A/B testing. No telemetry. No Firebase. No Google Play Services. No Google ML Kit.

The app is distributed as a standalone APK via GitHub Releases (and, in future, F-Droid). We do not receive any signal when you install, run, or use the app.

---

## Backups

Automatic Android Cloud Backup and device-to-device transfer are **excluded** via:

```xml
<application android:allowBackup="false" ... >
    <data-extraction-rules>
        <cloud-backup><exclude .../></cloud-backup>
        <device-transfer><exclude .../></device-transfer>
    </data-extraction-rules>
```

Manual backups created via the in-app Export feature are encrypted with a passphrase **you** choose (Argon2id KDF + AES-GCM). We have no way to recover this passphrase. If you lose it, the backup is unrecoverable.

---

## Children

PaisaVault is not directed at children under 13 (or 16, per jurisdiction). It contains no advertising, no social features, no data collection. That said, there is no targeted audience enforcement — a parent may choose to let a minor use it.

---

## Data retention

All data is stored **only on your device**. It is retained as long as you keep the app installed. Uninstalling the app removes all data.

There is no backend to retain data.

---

## Your rights

Because we process nothing, standard data-principal rights (access, correct, erase, portability) are all satisfied by:

- **Access**: use the app, or export your encrypted backup
- **Correct**: edit transactions in the app
- **Erase**: uninstall the app, or tap Settings → Advanced → Erase all data
- **Portability**: export CSV (encrypted) or full DB backup

No request is needed. No form is needed. No account is needed because you never created one.

---

## Compliance with jurisdictional laws

### India — DPDP Act 2023

PaisaVault operates under the **"personal or domestic purpose"** exemption in § 3(c)(ii) — since processing happens entirely on the user's device for the user's own benefit, the Act does not impose data-fiduciary obligations. We are not a data fiduciary; you are your own data principal.

### EU — GDPR

Since no personal data leaves the device and no identifier is collected, the GDPR generally does not apply to this app as distributed. Users remain data controllers of their own local data.

### Other jurisdictions

Same principle: local-only, no collection, no transfer, no third parties. Consult local law for specifics.

---

## Changes to this policy

This policy will be updated if the app ever changes its data practices (e.g., if a future version adds network features). Every change will be:

- Announced in the relevant release's CHANGELOG
- Committed to this file in git (full history visible)
- Never retroactive — previous versions remain governed by the policy at their release

---

## Contact

Questions: open a [GitHub Discussion](https://github.com/HemantBK/Expense-Tracker/discussions).

Security concerns: see [SECURITY.md](SECURITY.md) (do not use public issues).

---

## Audit

Because PaisaVault is 100% open source, you do not have to trust this policy. You can verify it:

- Read the source: https://github.com/HemantBK/Expense-Tracker
- Inspect the APK manifest: `aapt dump permissions app-release.apk`
- Inspect the APK classes: `apkanalyzer dex packages app-release.apk`
- Run MobSF on the APK
- Confirm no network traffic: enable Android's Private DNS logging or a local firewall, then use the app

If our code contradicts this policy, the code is the bug — please report it per SECURITY.md.
