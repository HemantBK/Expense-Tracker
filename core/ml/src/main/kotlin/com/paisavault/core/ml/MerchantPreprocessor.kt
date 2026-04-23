// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml

/**
 * Normalizes raw merchant strings pulled from bank/UPI SMS into something the classifier
 * can reason about. Example: `UPI/ZOMATO/FOOD-1234` -> `zomato food`.
 *
 * Reference Python implementation must match this behavior byte-for-byte — see
 * `ml-training/preprocessor.py`.
 */
internal object MerchantPreprocessor {

    private val noiseTokens = Regex(
        """(?i)\b(upi|pos|vpa|neft|imps|nach|payee|ref|txn|trf|to|at|from|pay|via)\b""",
    )

    private val digitRuns = Regex("""\d{3,}""")
    private val punctuation = Regex("""[\\/_\-:@.,;|]+""")
    private val whitespace = Regex("""\s+""")

    fun normalize(raw: String): String = raw
        .lowercase()
        .replace(punctuation, " ")
        .replace(noiseTokens, " ")
        .replace(digitRuns, " ")
        .replace(whitespace, " ")
        .trim()
}
