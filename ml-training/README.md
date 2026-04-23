# ml-training

Python side-project that trains the merchant classifier shipped by `:core:ml`.

See [MODEL_CARD.md](MODEL_CARD.md) for model details, intended use, and known biases.
Design rationale lives in [docs/adr/0007](../docs/adr/0007-pure-kotlin-logreg-categorizer.md).

---

## Layout

```
ml-training/
├── data/
│   └── merchants.csv          # seed labeled data; extend from your own exports
├── preprocessor.py            # mirrors MerchantPreprocessor.kt
├── hashing.py                 # mirrors HashingVectorizer.kt (java String.hashCode)
├── train.py                   # trains LR, emits model_v1.json
├── requirements.txt
├── MODEL_CARD.md
└── README.md  (this file)
```

## Quick start

```bash
cd ml-training
python -m venv .venv
source .venv/bin/activate       # Windows: .venv\Scripts\activate
pip install -r requirements.txt
python train.py
```

Output: `../core/ml/src/main/assets/model_v1.json`. Commit that file; the Android
runtime loads it at first categorization. If the file is absent, the rule-based
fallback alone is used.

## Retraining from your own transactions

From the running app, **Settings → Advanced → Export training data** writes a
CSV of your verified transactions to a folder you pick (the feature ships in
Phase 3.1 polish — the use case `ExportTrainingDataUseCase` is already wired).
Copy that file into `data/merchants.csv`, merge with the seed if you wish, and
re-run `python train.py`.

## Reproducibility

`train.py` seeds Python's `random` and NumPy with 42. Same CSV in = same model
out. `requirements.txt` is pinned. Commit the resulting `model_v1.json` so every
developer and CI run sees the same weights.

## Verifying hash parity (Python vs Kotlin)

```bash
python hashing.py       # runs internal smoke test
python preprocessor.py  # runs internal smoke test
```

In Kotlin: run `./gradlew :core:ml:testDebugUnitTest`. The
`HashingVectorizerTest` asserts the same fixtures.

## No proprietary dependencies

scikit-learn (BSD-3), NumPy (BSD-3), pandas (BSD-3). See the repo's FOSS policy
([docs/adr/0007](../docs/adr/0007-pure-kotlin-logreg-categorizer.md) and
[BUILD.md § 33](../BUILD.md#33-licensing--foss-compliance)).
