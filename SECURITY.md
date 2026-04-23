# Security Policy

PaisaVault handles financial data. Security is a core feature, not a feature request.

---

## Supported versions

Only the **latest tagged release** receives security fixes. Pre-release (`v0.x.y`) versions are fixed on a best-effort basis.

| Version | Supported |
|---|---|
| Latest stable (`v1.x.y`) | ✅ |
| Previous stable | ✅ for 30 days after new major release |
| Pre-1.0 (`v0.x.y`) | Best effort |

---

## Reporting a vulnerability

**Do not file a public GitHub issue for security problems.** Public disclosure before a fix exposes users.

### Preferred: private report via GitHub

1. Go to https://github.com/HemantBK/Expense-Tracker/security/advisories
2. Click **Report a vulnerability**
3. Include: affected version, reproduction steps, impact assessment, suggested fix (if any)

### Alternative: encrypted email

- Email: *(to be set by maintainer before first release — e.g., `security@<maintainer-domain>`)*
- PGP key: *(publish before first release at `https://github.com/HemantBK.gpg` or in repo as `keys/security.asc`)*

### What to expect

- **Acknowledgement**: within 72 hours
- **Initial assessment**: within 7 days
- **Fix timeline**: target 30 days for critical, 90 days for moderate
- **Disclosure**: coordinated — we will agree on a disclosure date before publishing

### Scope

In scope:
- Cryptographic weaknesses in local storage, backup, or key management
- Authentication bypass (biometric lock, auto-lock)
- Code execution via crafted SMS or malformed input
- Data leakage via logs, backups, or IPC
- Supply-chain compromise of dependencies
- Permission escalation
- Tampering of release APK that passes signature verification

Out of scope:
- Issues requiring a rooted device with active attacker (device integrity is the user's OS)
- Physical access with user-unlocked device (defense in depth only; not a bug)
- Social engineering of users
- Weaknesses in third-party banks' SMS content
- Theoretical attacks without a proof-of-concept

---

## Security architecture summary

- Data at rest: **AES-256 via SQLCipher**; passphrase derived from **Android Keystore**-held key (hardware-backed / StrongBox where available)
- App access: **BiometricPrompt (`BIOMETRIC_STRONG`)** with device-credential fallback; 60-second background auto-lock
- Task-switcher privacy: `FLAG_SECURE` on all activities
- Network: no `INTERNET` permission in v1.0; when added, HTTPS-only + certificate pinning
- Backup: user-passphrase-encrypted (Argon2id KDF + AES-GCM); excluded from Google cloud backup and device transfer
- Build: R8 full mode, APK Signature Scheme v3, reproducible, signed commits required
- Supply chain: Gradle dependency verification (committed checksums), OWASP Dependency-Check nightly, signed tags, no proprietary Google SDKs

Full threat model and mitigations in [BUILD.md § 23](BUILD.md#23-security-implementation).

---

## Responsible disclosure

Reporters acting in good faith will be credited in the release notes (with consent). We do not offer a bug bounty (this is a non-commercial project), but will publicly thank contributors.

We ask that reporters:

- Do not exploit the vulnerability beyond what is necessary to demonstrate impact
- Do not access, modify, or delete data belonging to others
- Do not publicly disclose before the agreed coordinated-disclosure date
- Give us reasonable time to fix before disclosure

---

## Our commitments

- We will respond promptly
- We will not take legal action against good-faith researchers
- We will credit you (unless you prefer anonymity)
- We will publish a CVE for significant issues
- We will transparently describe the impact and fix in the release notes
