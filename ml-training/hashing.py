# SPDX-License-Identifier: GPL-3.0-or-later
# Copyright (c) 2026 PaisaVault contributors
"""
Hashing vectorizer that matches `HashingVectorizer.kt`.

Uses Java's String.hashCode algorithm so features hash identically on both sides of the
train/serve boundary without shipping a vocabulary. sklearn's own HashingVectorizer uses
MurmurHash3 which is much harder to reproduce exactly in the JVM — hence our own hash.
"""

from __future__ import annotations
from collections import Counter
from math import sqrt
from typing import Dict, Iterable


def java_string_hash(s: str) -> int:
    """Replicates java.lang.String.hashCode() — 32-bit signed."""
    h = 0
    for ch in s:
        # Java uses `h * 31 + ch` with 32-bit overflow.
        h = (h * 31 + ord(ch)) & 0xFFFFFFFF
    # Convert unsigned to signed 32-bit
    if h & 0x80000000:
        h -= 0x1_0000_0000
    return h


def bucket(ngram: str, n_features: int) -> int:
    h = java_string_hash(ngram)
    m = h % n_features
    return m + n_features if m < 0 else m


def char_ngrams(text: str, ngram_range: Iterable[int]) -> Iterable[str]:
    padded = f" {text} "
    for n in ngram_range:
        if len(padded) < n:
            continue
        for i in range(len(padded) - n + 1):
            yield padded[i : i + n]


def transform(text: str, n_features: int, ngram_range: Iterable[int]) -> Dict[int, float]:
    if not text:
        return {}
    counts: Counter[int] = Counter()
    for gram in char_ngrams(text, ngram_range):
        counts[bucket(gram, n_features)] += 1
    if not counts:
        return {}
    norm = sqrt(sum(c * c for c in counts.values()))
    if norm <= 0:
        return {}
    return {idx: c / norm for idx, c in counts.items()}


if __name__ == "__main__":
    # Smoke test — must match Kotlin HashingVectorizerTest
    feats = transform("zomato", n_features=1024, ngram_range=range(3, 5))
    assert sum(v * v for v in feats.values()) - 1.0 < 1e-5
    print("hashing ok; features:", len(feats))
