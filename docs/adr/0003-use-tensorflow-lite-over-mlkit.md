# ADR-0003: Use TensorFlow Lite (not ML Kit) for on-device ML

- **Status:** Accepted
- **Date:** 2026-04-23
- **Deciders:** project lead
- **Tags:** ml, foss, privacy

## Context and problem

PaisaVault needs on-device machine learning for merchant-to-category classification (Phase 3), optional entity extraction, and receipt OCR (Phase 4). We need to choose an ML stack that:

- Runs fully on-device (no cloud inference)
- Is free and open-source (FOSS policy — ADR-0007)
- Has a maintained Android integration
- Supports models we can train ourselves

## Decision drivers

- FOSS-only policy for runtime dependencies
- F-Droid eligibility (disqualifies Google Play Services and most Google "free" SDKs)
- User privacy: the ML inference must not phone home
- Model portability: we want to train on a laptop and ship the model, not rely on a remote API

## Considered options

1. **TensorFlow Lite (core)** — Apache 2.0, open-source runtime, bring-your-own models
2. **Google ML Kit** — free-to-use but proprietary (closed-source SDK)
3. **ONNX Runtime Mobile** — MIT, open-source, supports more model formats
4. **PyTorch Mobile** — BSD, open-source, but larger footprint

## Decision

**Chosen option: Option 1 — TensorFlow Lite.**

TFLite is the most mature on-device ML runtime on Android, is Apache 2.0, and gives us full control over the model pipeline. We train models in Python (sklearn / TF) and convert via `TFLiteConverter`. The runtime is ~300 KB; models are ours to size.

For OCR specifically (a separate sub-decision, but recorded here for context), we use **Tesseract4Android** (Apache 2.0) rather than ML Kit Text Recognition (proprietary).

## Consequences

### Positive
- Fully FOSS — passes F-Droid inclusion criteria
- No Google Play Services dependency
- No network calls for inference
- Full control over model: size, quality, retraining cadence
- Works on devices without GMS (e.g., /e/OS, LineageOS, GrapheneOS)

### Negative / trade-offs
- We must train and maintain our own models (vs ML Kit's pre-trained convenience)
- Initial model quality will be worse than ML Kit's best-in-class offerings
- Tokenization / preprocessing is our responsibility
- Active-learning loop requires its own engineering

### Neutral
- Model size grows over time; must monitor APK size budget

## Options considered in detail

### Option 1 — TFLite
Chosen. Best balance of FOSS + maturity + ecosystem.

### Option 2 — Google ML Kit
Rejected. The SDK is closed-source and pulls in Google Play Services dependencies. Disqualified by our FOSS policy (ADR-0007). Users on GMS-free devices would not be able to run the app with these features.

### Option 3 — ONNX Runtime Mobile
Good FOSS alternative. Rejected due to: smaller community on Android, fewer examples, and the fact that our primary training path (sklearn → TF) is more direct to TFLite than to ONNX. If TFLite ever becomes unmaintained, we can revisit.

### Option 4 — PyTorch Mobile
Rejected. Runtime is larger (~5 MB), and we have no PyTorch-trained models planned. Not a good fit.

## Validation

- TFLite runtime APK impact: ≤ 500 KB (measured in Phase 3 PR).
- Merchant categorizer inference p95: ≤ 50 ms on Pixel 6a.
- FOSS compliance CI task passes — no `com.google.mlkit.*` in release classpath.
- App installs and runs on a GMS-free device (tested manually before v1.0).

## Links

- TensorFlow Lite: https://www.tensorflow.org/lite
- Tesseract4Android: https://github.com/adaptech-cz/Tesseract4Android
- Why no ML Kit on F-Droid: https://f-droid.org/docs/Inclusion_Policy/
- Related: [ADR-0007](0007-foss-only-policy.md) — FOSS-only policy
