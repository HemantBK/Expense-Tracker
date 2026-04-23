# Model Card — Merchant Classifier v1

## Overview

- **Model**: Multinomial logistic regression on hashed character n-grams (3–5).
- **Input**: raw merchant text extracted from bank/UPI SMS.
- **Output**: one of 8 categories (see below) with a softmax confidence score.
- **Size**: ~40 KB on disk as JSON.
- **Inference**: pure-Kotlin, target p95 < 5 ms on Pixel 6a, no GPU or TFLite needed.

## Intended use

- On-device categorization of small user expense-tracker app.
- Users can correct predictions; corrections become future training data.
- NOT for automated financial decisioning, credit scoring, fraud detection, or
  any production scenario with downstream consequences for the user or third parties.

## Training data

- Seed: `data/merchants.csv` — ~150 hand-curated (merchant, category) pairs covering
  common Indian merchants and UPI apps.
- Sources: public brand lists, not scraped from user data.
- Label space: `Food`, `Transport`, `Shopping`, `Bills`, `Entertainment`, `Health`,
  `Groceries`, `Other`.
- Split: 80 / 20 stratified train / test (seed 42).

Per-user retraining augments the seed with the user's own verified transactions
(exported locally via the app; never uploaded). That data never leaves the user's
device unless the user chooses to commit it back to this repository.

## Metrics

To be filled in by `train.py` on your machine. Expected baseline on seed data:

- Top-1 accuracy: ~0.85–0.92
- Per-class F1: balanced across categories via `class_weight='balanced'`

Publish the classification report printout + confusion matrix in this section after
the first training run.

## Known biases and limitations

- Strongly Indian: merchant names in other geographies (e.g., European retailers)
  will fall back to rule mismatches and route to `Other`.
- Character-n-gram features are sensitive to spelling variations (`ZOMATO` vs
  `zomato kitchen`), mitigated by preprocessing but not eliminated.
- Out-of-vocabulary merchants will often score below `MIN_USABLE_CONFIDENCE`
  (0.4); those land in the user-review queue, which is the correct behavior.
- The model does not attempt to disambiguate `AMAZON` grocery orders
  from `AMAZON` shopping — a single merchant maps to a single category.

## FOSS & privacy

- Trained entirely with FOSS tools (scikit-learn / NumPy / pandas).
- Bundled weights are covered by the repository's GPL-3.0-or-later license.
- No proprietary model is shipped; retraining is fully reproducible with
  `requirements.txt`.
- No user data leaves the device unless the user explicitly exports and shares it.

## Changelog

### v1 — initial release (Phase 3)

- Seed vocabulary: 150 Indian merchants + UPI apps.
- Features: 1024-dim hashing vectorizer, char n-gram 3–5.
- Classifier: sklearn `LogisticRegression` with `class_weight='balanced'`.
