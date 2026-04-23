# ADR-0007: Pure-Kotlin hashing logistic regression over TFLite for merchant categorization

- **Status:** Accepted
- **Date:** 2026-04-23
- **Deciders:** project lead
- **Tags:** ml, architecture, performance

## Context and problem

Phase 3 needs an on-device merchant → category classifier. The original plan in
BUILD.md § 12 listed three options:

- **Option A** — Character n-gram TF-IDF + Logistic Regression (< 500 KB model)
- **Option B** — MobileBERT fine-tune (~25 MB)
- **Option C** — Small sentence-embedding + kNN (middle ground)

Option A is far cheaper than the other two for a classification task whose inputs are
2–5 token merchant strings. The original note allowed "hand-port as hash-vectorizer in
Kotlin" as a valid delivery path for Option A, bypassing the TFLite runtime entirely.

The question here: do we run Option A via **TFLite** (as the common MAANG playbook
assumes), or **pure Kotlin** (shipping weights as JSON)?

## Decision drivers

- Inference is trivially linear algebra — no convolutions, no attention
- Model is tiny (~40 KB of weights); TFLite runtime is ~300 KB we don't need for this
- Debuggability: a pure Kotlin implementation is steppable in the IDE
- Portability: pure Kotlin keeps the door open for a Kotlin Multiplatform iOS port
  without needing to ship a separate TFLite iOS binary and converted Core ML model
- FOSS: both options are FOSS-compatible (both are Apache 2.0 or equivalent)
- Future-proofing: if we later need transformers or embeddings, we can re-introduce
  TFLite without breaking anything — the `MerchantCategorizer` interface hides it

## Considered options

1. **TFLite-backed Logistic Regression** — train in sklearn, convert to TF/keras, then
   to `.tflite`, ship the interpreter + flatbuffer model.
2. **Pure-Kotlin hashing LR** — port the vectorizer and classifier to Kotlin, ship a
   JSON weights file.
3. **Rules only (no ML)** — skip training entirely, ship only the rule-based
   categorizer from Phase 2.

## Decision

**Chosen option: Option 2 — Pure-Kotlin hashing LR.**

Architecture:

- `RuleCategorizer` runs first; known merchants get deterministic, high-confidence
  predictions in microseconds.
- `ModelCategorizer` (pure-Kotlin LR) runs for the rule misses.
- `CompositeCategorizer` picks the better of the two (or falls back to
  `Uncategorized` when confidence is too low).
- `MerchantClassifier` domain port is the only interface feature modules see.

The TFLite dependency is retained in `:core:ml`'s Gradle config for future upgrades
to larger models (e.g., sentence embeddings for "what did I spend on" NLQ in Phase 4),
but is unused in the Phase 3 classifier.

## Consequences

### Positive
- Inference p99 under 5 ms on mid-tier devices
- No native library weight (~0 bytes over the Kotlin stdlib for the LR path)
- Training reproducibility: `ml-training/train.py` + seed CSV + fixed seed = same JSON
- Kotlin implementation is one short file per component (vectorizer, LR, loader)
- Graceful fallback: if `model_v1.json` is missing from assets, rules still work
- KMP-ready for a future iOS port

### Negative / trade-offs
- Porting hashing logic requires maintaining bit-exact parity between
  `HashingVectorizer.kt` and `hashing.py`. We enforce this with a choice of
  `java.lang.String.hashCode` (portable by JLS) instead of MurmurHash3 (needs
  reimplementation in both sides).
- Using `String.hashCode` is a slightly weaker hash than MurmurHash3 in theory; in
  practice, with 1024 buckets and < 10K distinct n-grams, collision rates are negligible.
- Future move to a transformer model will require reintroducing TFLite. Acceptable.

### Neutral
- The JSON weights file is larger than a flatbuffer equivalent (~40 KB vs ~25 KB),
  but the difference is a rounding error against the APK size budget.

## Options considered in detail

### Option 1 — TFLite-backed LR
Works but over-engineered. Converting sklearn → TF → TFLite adds training complexity
(`sklearn-onnx`, `onnx-tf`, `tf.lite.TFLiteConverter` toolchain is fragile across
version bumps). Adds ~300 KB of runtime we don't need. Rejected.

### Option 2 — Pure-Kotlin hashing LR
Chosen. See above.

### Option 3 — Rules only
Rejected for this phase: the point of Phase 3 is to learn from user corrections via
training data, and rules can't generalize. Rules remain as the fast path.

## Validation

- `HashingVectorizerTest` asserts determinism and L2-normalization
- `RuleCategorizerTest` asserts known merchant mappings
- `ml-training/hashing.py` has a smoke test asserting the same L2 property
- Manual: run `train.py`, confirm the classification report shows expected accuracy
  (>= 0.85 on seed data), install the APK, watch incoming SMS get categorized in the
  review queue with sensible labels

## Links

- BUILD.md § 12, § 26 — ML pipeline
- Related: [ADR-0003](0003-use-tensorflow-lite-over-mlkit.md) — TFLite over ML Kit
- Related: [ADR-0006](0006-sms-parsing-strategy.md) — rule-based parsing + ML
  categorization is the intended composition
