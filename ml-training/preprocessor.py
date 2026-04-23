# SPDX-License-Identifier: GPL-3.0-or-later
# Copyright (c) 2026 PaisaVault contributors
"""
Reference Python implementation of the merchant preprocessor.
MUST match `core/ml/src/main/kotlin/com/paisavault/core/ml/MerchantPreprocessor.kt`
byte-for-byte so training and inference see the same inputs.
"""

import re

_NOISE = re.compile(r"\b(upi|pos|vpa|neft|imps|nach|payee|ref|txn|trf|to|at|from|pay|via)\b", re.IGNORECASE)
_DIGIT_RUNS = re.compile(r"\d{3,}")
_PUNCT = re.compile(r"[\\/_\-:@.,;|]+")
_WS = re.compile(r"\s+")


def normalize(raw: str) -> str:
    text = raw.lower()
    text = _PUNCT.sub(" ", text)
    text = _NOISE.sub(" ", text)
    text = _DIGIT_RUNS.sub(" ", text)
    text = _WS.sub(" ", text).strip()
    return text


if __name__ == "__main__":
    # Smoke test
    assert normalize("UPI/ZOMATO/FOOD-1234") == "zomato food"
    assert normalize("POS-SWIGGY") == "swiggy"
    assert normalize("VPA:merchant@ybl") == "merchant ybl"
    print("preprocessor ok")
