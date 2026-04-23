#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-or-later
# Copyright (c) 2026 PaisaVault contributors
"""
Train the merchant -> category classifier.

Usage:
    python train.py                                 # default input/output paths
    python train.py --in data/merchants.csv \
                    --out ../core/ml/src/main/assets/model_v1.json \
                    --n-features 1024

Reads a CSV with columns `merchant,category`, trains a logistic regression over
hashed character n-grams, writes a JSON model file that the Android runtime can load
via `ModelWeights`.

The hashing function matches `hashing.py` and `HashingVectorizer.kt` exactly, so no
vocabulary file is shipped.
"""

from __future__ import annotations

import argparse
import json
import pathlib
import random
import sys
from collections import defaultdict

import numpy as np
import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, confusion_matrix
from sklearn.model_selection import train_test_split

from hashing import transform
from preprocessor import normalize


def _features_matrix(texts, n_features, ngram_range):
    rows = []
    for t in texts:
        sparse = transform(normalize(t), n_features=n_features, ngram_range=ngram_range)
        vec = np.zeros(n_features, dtype=np.float32)
        for idx, value in sparse.items():
            vec[idx] = value
        rows.append(vec)
    return np.vstack(rows)


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--in", dest="input_csv", default="data/merchants.csv")
    parser.add_argument("--out", dest="output_json",
                        default="../core/ml/src/main/assets/model_v1.json")
    parser.add_argument("--n-features", type=int, default=1024)
    parser.add_argument("--ngram-min", type=int, default=3)
    parser.add_argument("--ngram-max", type=int, default=5)
    parser.add_argument("--seed", type=int, default=42)
    args = parser.parse_args(argv)

    random.seed(args.seed)
    np.random.seed(args.seed)

    df = pd.read_csv(args.input_csv)
    df["merchant"] = df["merchant"].astype(str).str.strip()
    df["category"] = df["category"].astype(str).str.strip()
    df = df[(df["merchant"] != "") & (df["category"] != "")]
    labels = sorted(df["category"].unique())
    print(f"Training on {len(df)} samples across {len(labels)} categories: {labels}")

    ngram_range = range(args.ngram_min, args.ngram_max + 1)

    x_train_txt, x_test_txt, y_train, y_test = train_test_split(
        df["merchant"].values, df["category"].values,
        test_size=0.2, random_state=args.seed, stratify=df["category"].values,
    )

    x_train = _features_matrix(x_train_txt, args.n_features, ngram_range)
    x_test = _features_matrix(x_test_txt, args.n_features, ngram_range)

    clf = LogisticRegression(
        C=1.0,
        max_iter=500,
        class_weight="balanced",
        solver="lbfgs",
        multi_class="multinomial",
        random_state=args.seed,
    )
    clf.fit(x_train, y_train)

    y_pred = clf.predict(x_test)
    print("\n=== Classification report ===")
    print(classification_report(y_test, y_pred, digits=3))
    print("\n=== Confusion matrix ===")
    print(pd.DataFrame(
        confusion_matrix(y_test, y_pred, labels=clf.classes_.tolist()),
        index=clf.classes_.tolist(),
        columns=clf.classes_.tolist(),
    ))

    # Align weight rows with sorted label order for a deterministic JSON on disk.
    label_order = clf.classes_.tolist()
    weights = {
        "modelId": "merchant-cat",
        "version": 1,
        "nFeatures": args.n_features,
        "ngramMin": args.ngram_min,
        "ngramMax": args.ngram_max,
        "labels": label_order,
        "intercepts": clf.intercept_.astype(float).round(6).tolist(),
        "coefficients": [coef.astype(float).round(6).tolist() for coef in clf.coef_],
    }

    out_path = pathlib.Path(args.output_json)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text(json.dumps(weights, indent=2))
    print(f"\nWrote {out_path} ({out_path.stat().st_size // 1024} KB)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
