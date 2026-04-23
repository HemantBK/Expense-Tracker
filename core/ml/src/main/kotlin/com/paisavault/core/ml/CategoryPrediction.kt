// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml

/**
 * Output of the merchant categorizer.
 *
 * @property label Canonical category name from the training vocabulary (e.g. "Food").
 *                 Caller is responsible for resolving this to a category ID.
 * @property confidence 0.0..1.0. Under [ConfidenceThreshold.Low] the caller should treat
 *                      the prediction as "needs review" and leave [Transaction.userVerified]
 *                      false.
 * @property source which tier produced this prediction (for debugging / review UI).
 */
data class CategoryPrediction(
    val label: String,
    val confidence: Float,
    val source: Source,
) {
    enum class Source { Rule, Model, Fallback }

    companion object {
        val Uncategorized = CategoryPrediction("Other", 0f, Source.Fallback)
    }
}

object ConfidenceThreshold {
    const val High: Float = 0.85f
    const val Medium: Float = 0.6f
    const val Low: Float = 0.4f
}
