// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.model

import kotlin.math.exp
import kotlin.math.ln

/**
 * Multinomial logistic regression inference. Weights are [nLabels x nFeatures] row-major.
 * Sparse feature vector (index -> value) keeps inference O(nonzero * nLabels) instead of
 * O(nFeatures * nLabels).
 */
internal class LogisticRegression(
    private val labels: List<String>,
    private val intercepts: FloatArray,
    private val coefficients: Array<FloatArray>,
) {

    init {
        require(labels.size == intercepts.size) { "labels and intercepts length mismatch" }
        require(labels.size == coefficients.size) { "labels and coefficients length mismatch" }
    }

    /** Returns (label, softmax probability) for the top class. */
    fun predict(features: Map<Int, Float>): Prediction {
        val logits = FloatArray(labels.size)
        for (i in labels.indices) {
            var score = intercepts[i]
            val row = coefficients[i]
            for ((idx, value) in features) {
                if (idx in row.indices) score += row[idx] * value
            }
            logits[i] = score
        }
        val probs = softmax(logits)
        var bestIdx = 0
        for (i in probs.indices) if (probs[i] > probs[bestIdx]) bestIdx = i
        return Prediction(labels[bestIdx], probs[bestIdx])
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.max()
        var sum = 0.0
        val shifted = DoubleArray(logits.size)
        for (i in logits.indices) {
            shifted[i] = exp((logits[i] - max).toDouble())
            sum += shifted[i]
        }
        return FloatArray(logits.size) { i -> (shifted[i] / sum).toFloat() }
    }

    data class Prediction(val label: String, val probability: Float)

    @Suppress("unused")
    private fun logStabilityCheck(): Double = ln(MIN_STABLE)

    private companion object {
        const val MIN_STABLE = 1e-12
    }
}
