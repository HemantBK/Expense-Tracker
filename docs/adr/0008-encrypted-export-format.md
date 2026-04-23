# ADR-0008: Encrypted export — PBKDF2 + AES-GCM, no Argon2 dependency

- **Status:** Accepted
- **Date:** 2026-04-23
- **Deciders:** project lead
- **Tags:** security, export

## Context and problem

Users need a way to back up transaction history off-device. Since we promise local-first,
anything written to an external location must be encrypted with a user-held secret so that
the exported file is useless to anyone who doesn't have that secret.

BUILD.md § 13.3 originally listed Argon2id as the preferred KDF "via libsodium-jni (ISC
license) or PBKDF2 with ≥ 600k iterations as fallback". We chose the fallback here.

## Decision drivers

- FOSS-only: both options qualify
- Zero native dependencies: fewer ABIs to ship, smaller APK, simpler reproducible builds
- Adequate security for our threat model: exported file lands on user's own cloud drive
  or USB, attacker needs physical + cloud access + unknown passphrase
- Implementation risk: standard JCA is battle-tested; adding a native crypto lib is new
  attack surface and new build complexity

## Considered options

1. **Argon2id (via libsodium-jni or argon2kt)** — memory-hard KDF, resists GPU brute force
2. **PBKDF2-HMAC-SHA256 with 600k iterations** — CPU-bound, pure JDK
3. **scrypt (via bouncycastle)** — memory-hard, no native code
4. **No export / manual DB copy** — rejected (poor UX)

## Decision

**Chosen option: Option 2 — PBKDF2-HMAC-SHA256 with 600k iterations.**

Combined with AES-256-GCM for encryption + 16-byte salt + 12-byte IV + 16-byte GCM tag +
versioned "PV01" magic header. Passphrase length is enforced ≥ 8 characters in the UI
(a weak but enforced floor; real strength comes from user choosing well).

```
| 4 bytes "PV01"
| 4 bytes version (1)
| 4 bytes kdf iter count (600,000)
| 16 bytes salt
| 12 bytes GCM IV
| N bytes ciphertext + 16 byte GCM tag
```

## Consequences

### Positive
- Zero native code in the APK — every ABI just works
- Reproducible builds unaffected
- Argon2 upgrade path is trivial (bump version in header, add Argon2id branch that reads
  different header fields)
- 600k iterations takes ~1.5s on a Pixel 6a — acceptable for a one-off export action
- AES-GCM provides authenticated encryption; tamper is detected at decrypt time
- Wire format is self-describing (iteration count embedded), so future-us can raise the
  count without breaking existing backups

### Negative / trade-offs
- PBKDF2 is NOT memory-hard. A well-funded attacker with GPU or ASIC can brute-force
  substantially faster than against Argon2. For our threat model (personal backup files
  in user's own cloud) this is acceptable; for high-stakes export (e.g., corporate
  finance data) we'd upgrade.
- OWASP 2023 minimum is 600k — we're at the floor, not well above it

### Neutral
- Users who want a stronger KDF can wait for v1.1 which can add Argon2id as a second
  option via `argon2kt` (Apache 2.0, would add ~1 MB per ABI).

## Validation

- `PassphraseEncryptorTest` covers: roundtrip, wrong-passphrase failure, GCM tamper
  detection, magic-header validation, non-determinism across encryptions of the same
  plaintext.
- Manual: export on a device, transfer the `.pvxc` file, attempt decrypt with a
  reference Python script (shipped in `ml-training/` — noted for v1.1).

## Links

- OWASP password storage cheatsheet:
  https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
- AES-GCM nonce reuse risks:
  https://datatracker.ietf.org/doc/html/rfc5288
- BUILD.md § 13.3
