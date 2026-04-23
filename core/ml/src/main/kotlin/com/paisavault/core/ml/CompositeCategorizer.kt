// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml

import com.paisavault.core.ml.model.ModelCategorizer
import com.paisavault.core.ml.rules.RuleCategorizer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs the rule categorizer first (microseconds, high confidence for known merchants),
 * then the trained model if available. Returns whichever produces a prediction above
 * [ConfidenceThreshold.Low]; otherwise [CategoryPrediction.Uncategorized].
 */
@Singleton
class CompositeCategorizer @Inject constructor(
    private val ruleCategorizer: RuleCategorizer,
    private val modelCategorizer: ModelCategorizer,
) : MerchantCategorizer {

    override suspend fun categorize(merchant: String): CategoryPrediction {
        val rule = ruleCategorizer.categorize(merchant)
        if (rule.confidence >= ConfidenceThreshold.Medium) return rule

        val model = modelCategorizer.categorize(merchant)
        return when {
            model.confidence >= ConfidenceThreshold.Low -> model
            rule.confidence > model.confidence -> rule
            else -> CategoryPrediction.Uncategorized
        }
    }
}
