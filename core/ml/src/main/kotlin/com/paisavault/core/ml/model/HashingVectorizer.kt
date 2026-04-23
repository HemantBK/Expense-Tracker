// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.model

/**
 * Character-n-gram hashing vectorizer. Produces a sparse feature map from input text.
 *
 * Hash function: Java's `String.hashCode` (stable across JVMs and identical to what the
 * training-side Python implementation computes — see `ml-training/hashing.py`). Using a
 * portable hash means the training script and the runtime agree bit-for-bit on feature
 * indices without shipping a vocabulary file.
 */
internal class HashingVectorizer(
    private val nFeatures: Int,
    private val ngramRange: IntRange,
) {

    init {
        require(nFeatures > 0) { "nFeatures must be > 0" }
        require(!ngramRange.isEmpty()) { "ngramRange must be non-empty" }
        require(ngramRange.first >= 1) { "ngram min must be >= 1" }
    }

    /** Returns a map from feature index to L2-normalized weight. */
    fun transform(text: String): Map<Int, Float> {
        if (text.isEmpty()) return emptyMap()
        val padded = " $text " // edge-padding so n-grams capture word boundaries
        val counts = HashMap<Int, Int>()
        for (n in ngramRange) {
            if (padded.length < n) continue
            for (i in 0..padded.length - n) {
                val gram = padded.substring(i, i + n)
                val idx = bucket(gram)
                counts.merge(idx, 1) { a, b -> a + b }
            }
        }
        if (counts.isEmpty()) return emptyMap()
        // L2 normalize (standard for TF-IDF / HashingVectorizer(norm='l2') in sklearn)
        val norm = kotlin.math.sqrt(counts.values.sumOf { it.toDouble() * it }).toFloat()
        if (norm <= 0f) return emptyMap()
        return counts.mapValues { (_, c) -> c.toFloat() / norm }
    }

    private fun bucket(ngram: String): Int {
        // Java's contractual String.hashCode() — portable and matches our Python reference.
        val h = ngram.hashCode()
        // Mod via bit-masking positive value. Works for any nFeatures that is not a power of 2.
        val mod = h % nFeatures
        return if (mod < 0) mod + nFeatures else mod
    }
}
