# ADR-0002: Use SQLCipher for local database encryption

- **Status:** Accepted
- **Date:** 2026-04-23
- **Deciders:** project lead
- **Tags:** security, data

## Context and problem

PaisaVault stores user financial transactions on the device. Android's full-disk encryption protects data when the device is powered off or the lock screen is active. It does not protect against:

- Malicious apps with elevated privileges
- `adb backup` extraction
- Forensic recovery from sold/discarded devices where user data was not wiped
- Rooted devices with active attackers

We need **app-level encryption at rest** as defense in depth.

## Decision drivers

- Security is a core product promise
- Must integrate with Room (we already chose Room for type-safe data access)
- Must be FOSS (OSI-approved or equivalent)
- Must be maintained and have a reasonable CVE history
- Key must be hardware-backed where possible (Android Keystore)

## Considered options

1. **SQLCipher for Android (Community Edition)** — BSD-style license, Zetetic LLC
2. **Jetpack Security EncryptedFile + plain SQLite** — AndroidX, Apache 2.0
3. **Homegrown field-level encryption** — application-layer AES-GCM
4. **No app-level encryption** — rely on Android FDE

## Decision

**Chosen option: Option 1 — SQLCipher for Android (Community Edition).**

SQLCipher is the battle-tested solution for transparent SQLite encryption. It has a clean Room integration via `SupportFactory`, is FOSS (BSD-style license), and the 256-bit passphrase lives only in the Android Keystore (never in plaintext in app memory longer than necessary).

## Consequences

### Positive
- All tables and indexes encrypted transparently
- Room's type safety preserved
- Key rotation possible via `PRAGMA rekey`
- Well-documented threat model and failure modes
- Widely deployed (Signal, ProtonMail, etc.)

### Negative / trade-offs
- ~3 MB added to APK size
- ~10–20% query overhead vs plain SQLite (acceptable for our workload)
- Initial DB open requires passphrase retrieval from Keystore → slight cold-start delay (~20–50 ms)
- Room schema migration tests require SQLCipher-aware test utilities

### Neutral
- Migration between encrypted and unencrypted DB is non-trivial (we'll never need it — always encrypted from v0.1)

## Options considered in detail

### Option 1 — SQLCipher for Android
Chosen. See above.

### Option 2 — Jetpack Security EncryptedFile + plain SQLite
Rejected. Encrypting the DB file as a blob means it must be decrypted to a temp location on every access, or held in memory. SQLite's write-ahead log and journal files also need handling. Significantly more error-prone than SQLCipher's page-level encryption.

### Option 3 — Homegrown field-level encryption
Rejected. Easy to get wrong. Indexes on encrypted fields don't work. Queries become painful. The only "advantage" — not depending on SQLCipher — is not worth the risk.

### Option 4 — No app-level encryption
Rejected. Relying only on Android FDE exposes users to `adb backup` extraction, sold-device recovery, and the unrooted-but-compromised-app scenarios.

## Validation

- `adb backup -apk -noshared com.paisavault` test: produces an archive containing no readable transaction data.
- Cold-start time with encrypted DB open: ≤ 500 ms p95 on Pixel 6a (budget in BUILD.md § 20).
- Query p95 for a month's transactions: ≤ 50 ms.
- Penetration test via MobSF before each release: no critical findings related to storage.

## Links

- SQLCipher for Android: https://www.zetetic.net/sqlcipher/
- Room + SQLCipher integration: https://github.com/commonsguy/cw-room/tree/master/Encrypted
- Jetpack Security: https://developer.android.com/jetpack/androidx/releases/security
