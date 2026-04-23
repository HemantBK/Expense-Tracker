# ADR-0006: Rule-based SMS parsing with optional ML, not ML-first

- **Status:** Accepted
- **Date:** 2026-04-23
- **Deciders:** project lead
- **Tags:** data, ml, security, parsing

## Context and problem

Indian bank and UPI SMS formats are heterogeneous but structurally predictable. We must extract amount, merchant, date, and transaction type from these messages reliably. We have three realistic approaches:

- **Pure rule-based (regex)** — a registry of per-bank rules
- **Pure ML-based (on-device classifier + entity extractor)** — one model handles everything
- **Hybrid (rules first, ML categorizes merchant)** — rules for extraction, ML for the subjective step

## Decision drivers

- SMS is hostile input — parsing code must be ReDoS-safe and amount-capped
- Formats are mostly predictable but change occasionally (bank tweaks template)
- We own zero training data at launch — an ML-first approach would ship broken
- Users want to know *why* a transaction was parsed; rules are auditable
- Privacy: no off-device parsing
- F-Droid compatibility: no proprietary SDK

## Considered options

1. **Pure rule-based** — regex per bank, no ML
2. **Pure ML** — single model parses everything end-to-end
3. **Hybrid: rules for extraction + ML for merchant→category**

## Decision

**Chosen option: Option 3 — Hybrid.**

Phase 2 ships rules only (the extraction half). Phase 3 adds the ML merchant→category classifier on top of the parsed merchant string. This means:

- Extraction (amount, merchant raw, date, type) is 100% rule-based and deterministic
- Categorization (merchant → Food / Transport / etc.) is ML-based, on-device TFLite
- Every parse flows through the review queue before counting as user-verified — ML errors are correctable

## Consequences

### Positive
- Works on day one, no training data needed
- Every parse is explainable via its rule (review screen can show "HDFC debit rule matched with 0.9 confidence")
- Additional banks = new rule file, no retraining
- Safe against ReDoS (bounded regex, 2KB body limit)
- Amount sanity caps (≤ 10 crore) prevent malformed-SMS DoS of the UI
- New rules can be added by contributors without touching ML pipeline

### Negative / trade-offs
- Rules drift when banks change templates — ongoing maintenance
- Per-bank regex coverage is never 100% — genuine misses will exist
- Users with obscure banks see nothing parsed for them
- Hand-written regex is less resilient to format drift than a ML model might be

### Neutral
- Review queue is mandatory for Phase 2 (no auto-confirm yet) because false-positive rate is unknown on your inbox
- We accept that some SMS will not parse — those just don't get a transaction, which is safer than a wrong transaction

## Options considered in detail

### Option 1 — Pure rule-based
Rejected as long-term strategy: categorization is subjective ("AMAZON" is Shopping, but sometimes it's electronics, sometimes groceries) and rules can't generalize. Accepted for extraction.

### Option 2 — Pure ML
Rejected: we would ship either (a) a generic pre-trained model that's proprietary or Google-only (fails FOSS policy), or (b) a weak custom model with no training data. Bad first impression.

### Option 3 — Hybrid
Chosen. See above.

## Validation

- Each rule has ≥ 5 anonymized sample SMS tests (`SmsParserTest`)
- Fuzz/ReDoS: oversized-body and amount-overflow tests in parser suite
- On real device: after enabling SMS permission, scanning historical inbox produces a finite list of parsed transactions in the review queue
- User-correction events in the review queue are persisted (Phase 3) as training samples for the ML categorizer

## Links

- Related: [ADR-0003](0003-use-tensorflow-lite-over-mlkit.md) — TFLite over ML Kit
- Related: [ADR-0005](0005-use-mvi-for-screen-state.md) — MVI
- BUILD.md § 25 — SMS Parsing Strategy
